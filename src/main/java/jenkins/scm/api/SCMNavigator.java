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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An API for discovering new and navigating already discovered {@link SCMSource}s within an organization.
 * An implementation does not need to cache existing discoveries, but some form of caching is strongly recommended
 * where the backing provider of repositories has a rate limiter on API calls.
 * @since 0.3-beta-1
 */
public abstract class SCMNavigator extends AbstractDescribableImpl<SCMNavigator> implements ExtensionPoint {

    /**
     * Replaceable pronoun of that points to a {@link SCMNavigator}. Defaults to {@code null} depending on the context.
     *
     * @since FIXME
     */
    public static final AlternativeUiTextProvider.Message<SCMNavigator> PRONOUN
            = new AlternativeUiTextProvider.Message<SCMNavigator>();

    protected SCMNavigator() {}

    /**
     * Looks for SCM sources in a configured place.
     * After this method completes, no further calls may be made to the {@code observer} or its child callbacks.
     * @param observer a recipient of progress notifications and a source of contextual information
     * @throws IOException if scanning fails
     * @throws InterruptedException if scanning is interrupted
     */
    public abstract void visitSources(@NonNull SCMSourceObserver observer) throws IOException, InterruptedException;

    /**
     * Returns the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports. There will always be
     * exactly one {@link SCMCategory#isUncategorized()} instance in the returned set.
     *
     * @return the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports.
     * @since FIXME
     */
    @NonNull
    public final Set<? extends SCMSourceCategory> getCategories() {
        Set<? extends SCMSourceCategory> result = getDescriptor().getCategories();
        if (result.size() > 1
                && MethodUtils.isOverridden(SCMNavigator.class, getClass(), "isCategoryEnabled", SCMSourceCategory.class)) {
            // if result has only one entry then it must be the default, so will never be filtered
            // if we didn't override the category enabled check, then none will be disabled
            result = new LinkedHashSet<SCMSourceCategory>(result);
            for (Iterator<? extends SCMSourceCategory> iterator = result.iterator(); iterator.hasNext(); ) {
                SCMSourceCategory category = iterator.next();
                if (!category.isUncategorized() && !isCategoryEnabled(category)) {
                    // only keep the enabled non-default categories
                    iterator.remove();
                }
            }
        }
        return result;
    }

    /**
     * Sub-classes can override this method to filter the categories that are available from a specific source. For
     * example a source type might be capable of having mainline branches, user branches, merge requests and
     * release tags while a specific instance of the source may be configured to only have mainline branches and
     * release tags.
     *
     * @param category the category.
     * @return {@code true} if the supplied category is enabled for this {@link SCMNavigator} instance.
     * @since FIXME
     */
    protected boolean isCategoryEnabled(@NonNull SCMSourceCategory category) {
        return true;
    }

    @Override
    public SCMNavigatorDescriptor getDescriptor() {
        return (SCMNavigatorDescriptor) super.getDescriptor();
    }

    /**
     * Get the term used in the UI to represent this kind of {@link SCMNavigator}. Must start with a capital letter.
     *
     * @return the term or {@code null} to fall back to the calling context's default.
     * @since FIXME
     */
    @CheckForNull
    public String getPronoun() {
        return AlternativeUiTextProvider.get(PRONOUN, this, getDescriptor().getPronoun());
    }

    /**
     * Fetches any actions that should be persisted for objects related to the specified owner. For example,
     * if a {@link Item} owns a specific {@link SCMNavigator}, then this method would be called to refresh
     * any {@link Action} instances of that {@link Item}.
     *
     * @param owner the owner of this {@link SCMNavigator}.
     * @param listener the listener to report progress on.
     * @return the map of {@link Action} instances to persist, keyed by the class of action. Keys with {@code null}
     * values indicate actions that should be removed if present.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     * @since FIXME
     */
    @NonNull
    public final Map<Class<? extends Action>, Action> fetchActions(@NonNull SCMNavigatorOwner owner,
                                                                   @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        return SCMSource.tidyActionMap(retrieveActions(owner, defaultListener(listener)));
    }

    /**
     * SPI for {@link #fetchActions(SCMNavigatorOwner, TaskListener)}. Fetches any actions that should be persisted for
     * objects related to the specified owner.
     *
     * @param owner the owner of this {@link SCMNavigator}.
     * @param listener the listener to report progress on.
     * @return the map of {@link Action} instances to persist, keyed by the class of action. Keys with {@code null}
     * values indicate actions that should be removed if present.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     * @since FIXME
     */
    @NonNull
    public Map<Class<? extends Action>, Action> retrieveActions(@NonNull SCMNavigatorOwner owner,
                                                                @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        return Collections.emptyMap();
    }

    /**
     * Turns a possibly {@code null} {@link TaskListener} reference into a guaranteed non-null reference.
     *
     * @param listener a possibly {@code null} {@link TaskListener} reference.
     * @return guaranteed non-null {@link TaskListener}.
     */
    @NonNull
    protected final TaskListener defaultListener(@CheckForNull TaskListener listener) {
        if (listener == null) {
            Level level;
            try {
                level = Level.parse(System.getProperty(getClass().getName() + ".defaultListenerLevel", "FINE"));
            } catch (IllegalArgumentException e) {
                level = Level.FINE;
            }
            return new LogTaskListener(Logger.getLogger(getClass().getName()), level);
        }
        return listener;
    }

}
