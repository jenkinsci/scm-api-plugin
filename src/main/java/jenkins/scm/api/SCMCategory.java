/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

/**
 * Base class for categories of SCM things.
 *
 * @param <T> the type of thing.
 * @since 2.0
 */
public abstract class SCMCategory<T> {

    /**
     * The name of this category, will likely be used as a path component in building URLs
     */
    private final String name;
    /**
     * The display name of this category or {@code null} to fall through to {@link #defaultDisplayName()}.
     */
    @CheckForNull
    private final Localizable displayName;
    /**
     * {@code true} if this is a special uncategorized category.
     */
    private final boolean uncategorized;

    /**
     * Constructs an uncategorized category with the specified display name.
     *
     * @param displayName the display name.
     */
    public SCMCategory(@CheckForNull Localizable displayName) {
        this.displayName = displayName;
        this.uncategorized = true;
        this.name = "default";
    }

    /**
     * Constructs a named category with the specified display name.
     *
     * @param name        the name of the category. This will likely be used as a path component in building URLs
     * @param displayName the display name.
     * @throws IllegalArgumentException if the supplied name is {@code default}
     */
    public SCMCategory(@NonNull String name, @CheckForNull Localizable displayName) {
        if ("default".equals(name)) {
            throw new IllegalArgumentException(
                    "Use the SCMCategory(Localizable) constructor to create an uncategorized category");
        }
        this.displayName = displayName;
        this.uncategorized = false;
        this.name = name;
    }

    /**
     * Gets the composite display name for a collection of {@link SCMCategory} instances.
     *
     * @param categories the categories.
     * @param <C>        the type of categories.
     * @return the composite display name for the categories
     */
    @NonNull
    public static <C extends SCMCategory<?>> Localizable toDisplayName(C... categories) {
        return toDisplayName(Arrays.asList(categories));
    }

    /**
     * Gets the composite display name for a collection of {@link SCMCategory} instances.
     *
     * @param categories the categories.
     * @param <C>        the type of categories.
     * @return the composite display name for the categories
     */
    @NonNull
    public static <C extends SCMCategory<?>> Localizable toDisplayName(@NonNull List<C> categories) {
        if (categories.size() == 1) {
            return categories.get(0).getDisplayName();
        }
        Set<Localizable> pronouns = new TreeSet<Localizable>(new Comparator<Localizable>() {
            @Override
            public int compare(Localizable o1, Localizable o2) {
                return o1.toString(Locale.ENGLISH).compareTo(o2.toString(Locale.ENGLISH));
            }
        });
        for (SCMCategory category : categories) {
            pronouns.add(category.getDisplayName());
        }
        Localizable result = null;
        for (Localizable next : pronouns) {
            if (result == null) {
                result = next;
            } else {
                result = Messages._SCMCategory_Join(result, next);
            }
        }
        return result;
    }

    /**
     * Gets the composite short url name for a collection of {@link SCMCategory} instances.
     *
     * @param categories the categories.
     * @param <C>        the type of categories.
     * @return the composite short url name for the categories
     */
    @NonNull
    public static <C extends SCMCategory<?>> String toShortUrl(C... categories) {
        return toShortUrl(Arrays.asList(categories));
    }

    /**
     * Gets the composite short url name for a collection of {@link SCMCategory} instances.
     *
     * @param categories the categories.
     * @param <C>        the type of categories.
     * @return the composite short url name for the categories
     */
    @NonNull
    public static <C extends SCMCategory<?>> String toShortUrl(List<C> categories) {
        if (categories.size() == 1) {
            return categories.get(0).getName().toLowerCase(Locale.ENGLISH);
        }
        Set<String> urlNames = new TreeSet<String>();
        for (SCMCategory category : categories) {
            urlNames.add(category.getName());
        }
        return StringUtils.join(urlNames, "_").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Partitions a list of {@link SCMCategory} instances by {@link #getName()}.
     *
     * @param categories the categories to group.
     * @param <T>        the base type of thing.
     * @param <C>        the base type of category.
     * @return the map of sorted {@link SCMCategory} instances keyed by {@link #getName()}.
     */
    @NonNull
    public static <T, C extends SCMCategory<T>> Map<String, List<C>> group(C... categories) {
        return group(Arrays.asList(categories));
    }

    /**
     * Partitions a collection of {@link SCMCategory} instances by {@link #getName()}.
     *
     * @param categories the categories to group.
     * @param <T>        the base type of thing.
     * @param <C>        the base type of category.
     * @return the map of sorted {@link SCMCategory} instances keyed by {@link #getName()}.
     */
    @NonNull
    public static <T, C extends SCMCategory<T>> Map<String, List<C>> group(@NonNull Iterable<C> categories) {
        Map<String, List<C>> result = new TreeMap<String, List<C>>();
        for (C c : categories) {
            String name = c.getName();
            List<C> l = result.get(name);
            if (l == null) {
                l = new ArrayList<C>();
                result.put(name, l);
            }
            l.add(c);
        }
        return result;
    }

    /**
     * Gets the url path component to use when building URLs from categories.
     *
     * @return the url path component;
     */
    @NonNull
    public final String getName() {
        return name;
    }

    /**
     * Get the term used in the UI to represent a collection of things in this kind of {@link SCMCategory}. Must be a
     * plural and start with a capital letter.
     *
     * @return the term for a collection of things in this kind of {@link SCMCategory}.
     */
    @NonNull
    public Localizable getDisplayName() {
        return (displayName == null ? defaultDisplayName() : displayName);
    }

    /**
     * The default display name.
     *
     * @return The generic term of a collection of things in this kind of {@link SCMCategory}.
     */
    @NonNull
    protected abstract Localizable defaultDisplayName();

    /**
     * Checks if this is the uncategorized category. The uncategorized category is special as {@link #isMatch(Object)}
     * will not be applied, rather the uncategorized category will pick up everything that has not been captured by
     * another
     * category.
     *
     * @return {@code true} if this is a catch-all category.
     */
    public final boolean isUncategorized() {
        return uncategorized;
    }

    /**
     * Checks if the supplied instance is a match for this {@link SCMCategory}.
     *
     * @param instance the instance to test.
     * @return {@code true} if the instance belongs to this {@link SCMCategory}.
     */
    public abstract boolean isMatch(@NonNull T instance);

    /**
     * Checks if the supplied instance is a match for this {@link SCMCategory}.
     *
     * @param instance   the instance to test.
     * @param categories ignored unless {@link #isUncategorized()} in which case it should be the list of categories.
     * @return {@code true} if the instance belongs to this {@link SCMCategory}.
     */
    public boolean isMatch(@NonNull T instance, @Nullable Iterable<? extends SCMCategory<T>> categories) {
        if (isUncategorized()) {
            if (categories != null) {
                for (SCMCategory<T> category : categories) {
                    if (!category.isUncategorized() && category.isMatch(instance)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return isMatch(instance);
        }
    }

}
