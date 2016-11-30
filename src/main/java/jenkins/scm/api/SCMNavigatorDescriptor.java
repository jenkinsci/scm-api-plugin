/*
 * The MIT License
 *
 * Copyright (c) 2015-2016 CloudBees, Inc.
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
import hudson.model.Descriptor;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import net.jcip.annotations.GuardedBy;
import org.jenkins.ui.icon.IconSpec;

/**
 * Definition of a kind of {@link SCMNavigator}.
 * @since 0.3-beta-1
 */
public abstract class SCMNavigatorDescriptor extends Descriptor<SCMNavigator> implements IconSpec {
    /**
     * The set of {@link SCMSourceCategory} singletons for this type of {@link SCMNavigator}
     * @since 2.0
     * @see #getCategories()
     */
    @GuardedBy("this")
    protected transient Set<SCMSourceCategory> categories;

    protected SCMNavigatorDescriptor() {}

    protected SCMNavigatorDescriptor(Class<? extends SCMNavigator> clazz) {
        super(clazz);
    }

    /**
     * A description of this kind of {@link SCMNavigator}. This description can contain HTML code but it is recommended
     * to use text plain in order to avoid how it should be represented.
     *
     * @return A string with the description. {@code TopLevelItemDescriptor#getDescription()}.
     * TODO: Replace to @link when the baseline is upgraded to 2.x
     *
     * @since 1.2
     */
    @NonNull
    public String getDescription() {
        return Messages.SCMNavigator_Description();
    }

    /**
     * Used to categorize this kind of {@link SCMNavigator}.
     *
     * @return A string with the category identifier. {@code TopLevelItemDescriptor#getCategoryId()}
     * TODO: Replace to @link when the baseline is upgraded to 2.x
     *
     * @since 1.2
     */
    @NonNull
    public String getCategoryId() {
        return "nested-projects";
    }

    /**
     * Represents a file path pattern to get the Item icon in different sizes.
     *
     * For example: plugin/plugin-shortname/images/:size/item.png, where {@code :size} represents the different
     * icon sizes used commonly in Jenkins project: 16x16, 24x24, 32x32 or 48x48
     *
     * @return A string or null if it is not defined. {@code TopLevelItemDescriptor#getIconFilePathPattern()}
     * TODO: Replace to @link when the baseline is upgraded to 2.x
     *
     * @since 1.2
     */
    @CheckForNull
    public String getIconFilePathPattern() {
        return null;
    }

    /**
     * Get the Item's Icon class specification e.g. 'icon-notepad'.
     * Note: do <strong>NOT</strong> include icon size specifications (such as 'icon-sm').
     *
     * @return The Icon class specification e.g. 'icon-notepad'.
     */
    @CheckForNull
    public String getIconClassName() {
        return null;
    }

    /**
     * Get the term used in the UI to represent this kind of {@link SCMNavigator}. Must start with a capital letter.
     *
     * @return the term or {@code null} to fall back to the calling context's default.
     * @since 2.0
     */
    @CheckForNull
    public String getPronoun() {
        return null;
    }

    /**
     * Returns the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports. There will always be
     * exactly one {@link SCMCategory#isUncategorized()} instance in the returned set.
     *
     * @return the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports.
     * @since 2.0
     * @see #createCategories()
     */
    @NonNull
    public synchronized final Set<SCMSourceCategory> getCategories() {
        if (categories == null) {
            Set<SCMSourceCategory> categories = new LinkedHashSet<SCMSourceCategory>();
            boolean haveDefault = false;
            for (SCMSourceCategory c: createCategories()) {
                if (c.isUncategorized()) {
                    if (!haveDefault) {
                        categories.add(c);
                        haveDefault = true;
                    }
                } else {
                    categories.add(c);
                }
            }
            if (!haveDefault) {
                categories.add(new UncategorizedSCMSourceCategory());
            }
            this.categories = Collections.unmodifiableSet(categories);
        }
        return categories;
    }

    /**
     * Creates the singleton {@link SCMSourceCategory} instances that this type of {@link SCMNavigator} is capable of
     * producing.
     * @return the singleton {@link SCMSourceCategory} instances for this type of {@link SCMNavigator}
     * @since 2.0
     * @see #getCategories()
     */
    @NonNull
    @GuardedBy("this")
    protected SCMSourceCategory[] createCategories() {
        return new SCMSourceCategory[]{new UncategorizedSCMSourceCategory()};
    }

    /**
     * Creates a default navigator, if there should be one displayed.
     * May be interpreted by {@code jenkins.branch.CustomOrganizationFolderDescriptor}.
     *
     * If returning non-null, you should also provide a {@link #getDescription()} implementation
     *
     * @param name a name supplied by the user which may be used as a hint for preconfiguration
     *
     * @return a navigator with a default configuration, or null if it should not be advertised this way
     */
    @CheckForNull
    public abstract SCMNavigator newInstance(@CheckForNull String name);

}
