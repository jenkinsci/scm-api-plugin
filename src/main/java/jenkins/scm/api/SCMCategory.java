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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
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
 * <blockquote>
 * A rose by any other name would smell as sweet?
 * </blockquote>
 * Naming things is hard. Naming things related to a source control system is even harder. It would be really nice
 * if all the source control systems could agree on a standard set or terminologies for how to name things to do with
 * source control systems. You are reading this and thinking "but how bad can it be?":
 * <ul>
 * <li><strong>Git</strong> - Git uses the following terminology:
 * <ul>
 * <li>Each versioned tree of source files in Git is called a "Commit"</li>
 * <li>The collection of semi-related commits in Git is called a "Repository"</li>
 * <li>Each commit has a history of previous commits. Each Git repository has a collection of references to commits.
 * Mutable references are called "Branches". Immutable references are called "Tags"</li>
 * <li>The remote place where all the Git repositories are kept is called a "Server"</li>
 * </ul>
 * So we have "Commits"; "Branches" and "Tags"; "Repositories"; and "Servers"
 * </li>
 * <li><strong>AccuRev</strong> - AccuRev uses the following terminology:
 * <ul>
 * <li>Accurev tracks a tree of files, each file has a "Version"</li>
 * <li>Each tree of files starts from a single initiation point and that tree's multiple evolutions are all tracked
 * in a single "Depot"</li>
 * <li>Each depot has a collection of references to version states of the tree at different points in time. Mutable
 * references are called "Streams". Immutable references are called "Snapshots"</li>
 * <li>The remote place where all the Accurev depots are kept is called a "Repository"</li>
 * </ul>
 * So we have "Versions"; "Streams" and "Snapshots"; "Depots"; and "Repositories"
 * </li>
 * <li><strong>Subversion</strong> -  Subversion is really a versioned filesystem and people use conventions to do
 * source control with the versioned
 * file system.
 * There are two common conventions:
 * <ul>
 * <li>Keep one project per repository</li>
 * <li>Keep multiple projects per repository, each with their own sub-tree</li>
 * </ul>
 * The other common convention is the use of {@code trunk} / {@code branches/___} / {@code tags/___} structure for
 * each project.
 * <p>
 * So, if following conventions you might have "commits"; "trunk", "branches" and "tags"; "projects" or
 * "repositories"; and "repositories" or "servers"
 * </li>
 * </ul>
 * So if a UI is calling something a collection of "Repositories" that may mean different things to different users
 * depending on the source control system they are using. It would be really good if the SCM API enabled the
 * implementations to provide their names and categories of names.
 * <p>
 * For example:
 * <ul>
 * <li>A GitHub implementation of the SCM API might return "Branches", "Pull Requests", and "Tags" as the different
 * types of {@link SCMHead} discovered by its {@link SCMSource}. It might return "Repositories" and "Forks" as the
 * different types of project discovered by its {@link SCMNavigator}.
 * </li>
 * </ul>
 * This class is the base class for the different kinds of categorizations. Currently the SCM API supports two
 * specializations:
 * <ul>
 * <li>{@link SCMHeadCategory} is used to categorize {@link SCMHead} instances returned by a {@link SCMSource}</li>
 * <li>{@link SCMSourceCategory} is used to categorize {@link SCMSource} instances returned by a
 * {@link SCMNavigator}</li>
 * </ul>
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
    public static <C extends SCMCategory<?>> Localizable toDisplayName(@NonNull final List<C> categories) {
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one category");
        }
        if (categories.size() == 1) {
            return categories.get(0).getDisplayName();
        }
        List<Localizable> localizables = new ArrayList<>(categories.size());
        for (C category : categories) {
            localizables.add(category.getDisplayName());
        }
        return new CompositeLocalizable(localizables);
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
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one category");
        }
        if (categories.size() == 1) {
            // the reference locale for Jenkins is english, so we build URLs in english
            return categories.get(0).getName().toLowerCase(Locale.ENGLISH); // force lowercase to align different impls
        }
        Set<String> urlNames = new TreeSet<>();
        for (SCMCategory category : categories) {
            urlNames.add(category.getName().toLowerCase(Locale.ENGLISH)); // force lowercase to align different impls
        }
        // the reference locale for Jenkins is english, so we build URLs in english
        return StringUtils.join(urlNames, "_");
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
        Map<String, List<C>> result = new TreeMap<>();
        for (C c : categories) {
            String name = c.getName();
            List<C> l = result.computeIfAbsent(name, k -> new ArrayList<>());
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

    /**
     * A {@link Localizable} that is the result of joining the set of terms.
     */
    private static class CompositeLocalizable extends Localizable {

        /**
         * Standardize serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * The terms to join.
         */
        private final List<Localizable> terms;

        /**
         * Constructor.
         *
         * @param terms the terms to join.
         */
        public CompositeLocalizable(List<Localizable> terms) {
            super(null, null);
            if (terms.size() < 2) {
                throw new IllegalArgumentException("Must have at least two Localizable instances to join");
            }
            this.terms = terms;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString(final Locale locale) {
            // put them in a set so that where translations overlap we can consolidate the terms
            Set<Localizable> pronouns = new TreeSet<>(new LocalizableComparator(locale));
            pronouns.addAll(terms);
            Iterator<Localizable> iterator = pronouns.iterator();
            Localizable result = iterator.next();
            while (iterator.hasNext()) {
                Localizable next = iterator.next();
                result = Messages._SCMCategory_Join(result, next);
            }
            return result.toString();
        }
    }

    /**
     * Compares {@link Localizable} instances based on a specified {@link Locale}.
     */
    private static class LocalizableComparator implements Comparator<Localizable>, Serializable {
        /**
         * Standardize serialization.
         */
        private static final long serialVersionUID = 1L;
        /**
         * The {@link Locale} to compare with.
         */
        private final Locale locale;

        /**
         * Constructor.
         *
         * @param locale the {@link Locale} to compare with.
         */
        public LocalizableComparator(Locale locale) {
            this.locale = locale;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Localizable o1, Localizable o2) {
            return o1.toString(locale).compareTo(o2.toString(locale));
        }
    }
}
