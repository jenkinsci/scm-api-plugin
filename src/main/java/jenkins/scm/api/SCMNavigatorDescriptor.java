/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

import hudson.model.Descriptor;
import hudson.model.TopLevelItemDescriptor;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Definition of a kind of {@link SCMNavigator}.
 * @since 0.3-beta-1
 */
public abstract class SCMNavigatorDescriptor extends Descriptor<SCMNavigator> {

    protected SCMNavigatorDescriptor() {}

    protected SCMNavigatorDescriptor(Class<? extends SCMNavigator> clazz) {
        super(clazz);
    }

    /**
     * A description of this kind of {@link SCMNavigator}. This description can contain HTML code but it is recommended
     * to use text plain in order to avoid how it should be represented.
     *
     * @return A string with the description.
     *
     * @since 1.2
     */
    @Nonnull
    public String getDescription() {
        return Messages.SCMNavigator_Description();
    }

    /**
     * Used to categorize this kind of {@link SCMNavigator}.
     *
     * @return A string with the category identifier.
     *
     * @since 1.2
     */
    @Nonnull
    public String getCategoryId() {
        return "nested-projects";
    }

    /**
     * Represents a file path pattern to get the Item icon in different sizes.
     *
     * For example: plugin/plugin-shortname/images/:size/item.png, where {@code :size} represents the different
     * icon sizes used commonly in Jenkins project: 16x16, 24x24, 32x32 or 48x48
     *
     * @return A string or null if it is not defined.
     *
     * @since 1.2
     */
    @CheckForNull
    public String getIconFilePathPattern() {
        return null;
    }

    /**
     * Creates a default navigator, if there should be one displayed.
     * May be interpreted by {@code jenkins.branch.SpecificDescriptor}.
     * If returning non-null, you may also provide a {@code newInstanceDetail} view as per {@link TopLevelItemDescriptor}.
     * @param name a name supplied by the user which may be used as a hint for preconfiguration
     * @return a navigator with a default configuration, or null if it should not be advertised this way
     */
    @CheckForNull
    public abstract SCMNavigator newInstance(@CheckForNull String name);

}
