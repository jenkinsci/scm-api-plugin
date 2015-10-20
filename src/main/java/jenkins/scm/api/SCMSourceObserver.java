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

import hudson.model.Item;
import hudson.model.TaskListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Callback used by {@link SCMNavigator}.
 * @since FIXME
 */
public abstract class SCMSourceObserver {

    /**
     * Indicates who is asking for sources.
     * @return a contextual item, typically a {@code OrganizationFolder}
     */
    @Nonnull
    public abstract SCMSourceOwner getContext();

    /**
     * Provides a way of reporting progress.
     * @return a logger
     */
    @Nonnull
    public abstract TaskListener getListener();

    /**
     * Declare that a new “project” such as a source repository has been found.
     * @param projectName a name of the project, such as a repository name within an organization; may be used as an {@link Item#getName}
     * @return a secondary callback to customize the project, on which you must call {@link ProjectObserver#complete}
     * @throws IllegalArgumentException if this {@code projectName} has already been encountered
     */
    @Nonnull
    public abstract ProjectObserver observe(@Nonnull String projectName) throws IllegalArgumentException;

    /**
     * Adds extra metadata about the overall organization.
     * Currently no metadata keys are defined; placeholder for description, icon, URL, etc.
     * @param key a predefined attribute name
     * @param value some value, of a type defined by the attribute, perhaps null if allowed by the attribute documentation
     * @throws IllegalArgumentException if the attribute name is unrecognized, or this attribute was already added
     * @throws ClassCastException if the attribute value is inappropriate for its type
     */
    public abstract void addAttribute(@Nonnull String key, @Nullable Object value) throws IllegalArgumentException, ClassCastException;

    /**
     * Nested callback produced by {@link #observe}.
     */
    public static abstract class ProjectObserver {

        /**
         * Adds a source repository to be used from a new project.
         * @param source a potential SCM source as in {@code MultiBranchProject.getSCMSources}; do not call {@link SCMSource#setOwner} on it
         */
        public abstract void addSource(@Nonnull SCMSource source);

        /**
         * Adds extra metadata about a specific project.
         * Currently no metadata keys are defined; placeholder for description, icon, URL, etc.
         * @param key a predefined attribute name
         * @param value some value, of a type defined by the attribute, perhaps null if allowed by the attribute documentation
         * @throws IllegalArgumentException if the attribute name is unrecognized, or this attribute was already added
         * @throws ClassCastException if the attribute value is inappropriate for its type
         */
        public abstract void addAttribute(@Nonnull String key, @Nullable Object value) throws IllegalArgumentException, ClassCastException;

        /**
         * To be called when finished defining one project.
         * @throws IllegalStateException may be thrown if called twice
         * @throws InterruptedException if processing of the final project was interrupted
         */
        public abstract void complete() throws IllegalStateException, InterruptedException;

    }

}
