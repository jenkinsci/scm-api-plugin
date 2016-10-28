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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.jvnet.localizer.Localizable;

/**
 * Base class for categories of {@link SCMHead}, for example: branches, tags, change requests, mainlines, features, etc.
 *
 * @since FIXME
 */
public abstract class SCMHeadCategory extends SCMCategory<SCMHead> {

    /**
     * Iterates {@link SCMSource} instances collecting the unique list of {@link SCMHeadCategory} instances.
     *
     * @param sources the {@link SCMSource} instances to iterate.
     * @return the list of unique {@link SCMHeadCategory} instances.
     */
    @NonNull
    public static List<SCMHeadCategory> collect(@NonNull Iterable<SCMSource> sources) {
        List<SCMHeadCategory> result = new ArrayList<SCMHeadCategory>();
        for (SCMSource source : sources) {
            CATEGORIES:
            for (SCMHeadCategory c : source.getCategories()) {
                for (SCMHeadCategory added : result) {
                    if (c == added) {
                        // we want uniqueness based on instance identity
                        continue CATEGORIES;
                    }
                }
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Ensures that the supplied list of categories includes the {@link UncategorizedSCMHeadCategory}.
     *
     * @param categories the list of {@link SCMHeadCategory} instances (must be mutable)
     * @return the supplied list for method chaining.
     */
    public static List<SCMHeadCategory> addUncategorizedIfMissing(List<SCMHeadCategory> categories) {
        for (SCMHeadCategory category : categories) {
            if (category.isUncategorized()) {
                return categories;
            }
        }
        categories.add(new UncategorizedSCMHeadCategory());
        return categories;
    }

    /**
     * Reduces a list of categories into a single composite category.
     *
     * @param categories the list of categories.
     * @return a composite category.
     */
    public static SCMHeadCategory union(List<SCMHeadCategory> categories) {
        if (categories.size() == 1) {
            return categories.get(0);
        }
        boolean includesCatchAll = false;
        final List<SCMHeadCategory> _categories = new ArrayList<SCMHeadCategory>(categories);
        for (SCMHeadCategory c : _categories) {
            if (c.isUncategorized()) {
                includesCatchAll = true;
                break;
            }
        }
        if (includesCatchAll) {
            final Localizable displayName = toDisplayName(_categories);
            return new SCMHeadCategory(displayName) {
                @NonNull
                @Override
                protected Localizable defaultDisplayName() {
                    return displayName;
                }

                @Override
                public boolean isMatch(@NonNull SCMHead instance) {
                    return true; // ignored as this is a catch-all
                }
            };
        } else {
            final Localizable displayName = toDisplayName(_categories);
            String name = toShortUrl(_categories);
            return new SCMHeadCategory(name, displayName) {
                @NonNull
                @Override
                protected Localizable defaultDisplayName() {
                    return displayName;
                }

                @Override
                public boolean isMatch(@NonNull SCMHead instance) {
                    for (SCMHeadCategory c : _categories) {
                        if (c.isMatch(instance)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    /**
     * Simplifies a collection of {@link SCMHeadCategory} replacing duplicates with their {@link #union(List)}.
     *
     * @param categories the collection of categories to simplify.
     * @return the map of simplified categories keyed by {@link #getName()}.
     */
    public static Map<String, SCMHeadCategory> simplify(Iterable<SCMHeadCategory> categories) {
        Map<String, SCMHeadCategory> result = new TreeMap<String, SCMHeadCategory>();
        for (Map.Entry<String, List<SCMHeadCategory>> entry : group(categories).entrySet()) {
            result.put(entry.getKey(), union(entry.getValue()));
        }
        return result;
    }

    /**
     * Collects the {@link SCMHeadCategory} instances from a set of {@link SCMSource} instances and simplifies the
     * result.
     *
     * @param sources the {@link SCMSource} instances
     * @return the map of simplified categories keyed by {@link #getName()}.
     * @see #simplify(Iterable)
     * @see #addUncategorizedIfMissing(List)
     * @see #group(Iterable)
     * @see #collect(Iterable)
     */
    public static Map<String, SCMHeadCategory> collectAndSimplify(@NonNull Iterable<SCMSource> sources) {
        return simplify(addUncategorizedIfMissing(collect(sources)));
    }

    /**
     * {@inheritDoc}
     */
    public SCMHeadCategory(@CheckForNull Localizable pronoun) {
        super(pronoun);
    }

    /**
     * {@inheritDoc}
     */
    public SCMHeadCategory(@NonNull String urlName, @CheckForNull Localizable pronoun) {
        super(urlName, pronoun);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected Localizable defaultDisplayName() {
        return Messages._SCMHeadCategory_DisplayName();
    }
}
