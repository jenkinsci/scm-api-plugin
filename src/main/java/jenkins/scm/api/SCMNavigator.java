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
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.TaskListener;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.TransientActionFactory;
import jenkins.scm.api.trait.SCMTrait;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * An API for discovering new and navigating already discovered {@link SCMSource}s within an organization.
 * An implementation does not need to cache existing discoveries, but some form of caching is strongly recommended
 * where the backing provider of repositories has a rate limiter on API calls.
 *
 * @since 0.3-beta-1
 */
public abstract class SCMNavigator extends AbstractDescribableImpl<SCMNavigator> implements ExtensionPoint {

    /**
     * Replaceable pronoun of that points to a {@link SCMNavigator}. Defaults to {@code null} depending on the context.
     *
     * @since 2.0
     */
    public static final AlternativeUiTextProvider.Message<SCMNavigator> PRONOUN
            = new AlternativeUiTextProvider.Message<>();

    /**
     * Cache of the ID of this {@link SCMNavigator}.
     * @since 2.0.1
     */
    private transient String id;

    /**
     * Constructor.
     */
    protected SCMNavigator() {
    }

    /**
     * Returns the ID of the thing being navigated.
     * <p>
     * The ID will typically be a composite of things like the server and the project/organization that the navigator
     * is scoped to.
     * <p>
     * For example, a GitHub navigator that is navigating repositories in a GitHub organization could construct
     * its ID as being the URL of the GitHub Server (to allow for GitHub Enterprise servers) and the name of the
     * organization.
     * <p>
     * The key criteria is that if two navigators have the same ID <strong>and</strong> they are both in the same
     * {@link SCMNavigatorOwner} then the results from
     * {@link #fetchActions(SCMNavigatorOwner, SCMNavigatorEvent, TaskListener)} should be not just equivalent but
     * {@link List#equals(Object)}.
     *
     * @return the ID of the thing being navigated by this navigator.
     * @since 2.0.1
     * @see #id()
     */
    @NonNull
    public final String getId() {
        if (id == null) {
            if (MethodUtils.isAbstract(getClass(), "id")) {
                // we need to ensure that upgrading the plugin does not change the digest
                // processing XML with a regex is usually a bad thing. In this case we have a sufficiently
                // strict set of requirements to match (shortname)@(version) and the constraints on the
                // shortname and version are such that we can have a high degree of certainty of a match
                // and even if we glom some legitimate text, the chances of a duplicate that is otherwise
                // the same but has a critical difference in a regular value that happens to contain
                // plugin="xys@abc" is sufficiently low.
                //
                // We can use a generated ID based on the initial serialization digest because any implementation
                // that does not have an id() override will not have a fetchActions() override either
                // so there will be no actions to retain anyway.
                //
                // Real implementations should not use this type of scheme for generating their id, rather
                // they should construct their id explicitly.
                id = getClass().getName() + "::" + Util.getDigestOf(Items.XSTREAM.toXML(this)
                        .replaceAll(" plugin=(('[^']+@[^']+')|(\"[^\"]+@[^\"]+\"))", ""));
            } else {
                id = getClass().getName() + "::" + id();
            }
        }
        return id;
    }

    /**
     * If implementations are using {@link DataBoundSetter} on fields that affect the {@link #id()} calculation then
     * those fields must call {@link #resetId()} if they may have invalidated the cached {@link #getId()}.
     *
     * @since 2.0.1
     */
    protected final void resetId() {
        id = null;
    }

    /**
     * Generates the ID of the thing being navigated from the configuration of this {@link SCMNavigator}.
     * <p>
     * The ID will typically be a composite of things like the server and the project/organization that the navigator
     * is scoped to.
     * <p>
     * For example, a GitHub navigator that is navigating repositories in a GitHub organization could construct
     * its ID as being the URL of the GitHub Server (to allow for GitHub Enterprise servers) and the name of the
     * organization.
     * <p>
     * The key criteria is that if two navigators have the same ID <strong>and</strong> they are both in the same
     * {@link SCMNavigatorOwner} then the results from
     * {@link #fetchActions(SCMNavigatorOwner, SCMNavigatorEvent, TaskListener)} should be not just equivalent but
     * {@link List#equals(Object)}.
     * <p>
     * If the results could be non-equal for navigators with the same ID then more detail needs to be encoded in the ID.
     *
     * @return the ID of the thing being navigated by this navigator.
     * @since 2.0.1
     * @see #resetId()
     * @see #getId()
     */
    @NonNull
    protected abstract String id();

    /**
     * Sets the traits for this navigator. No-op by default.
     * @param traits the list of traits
     */
    public void setTraits(@CheckForNull List<SCMTrait<? extends SCMTrait<?>>> traits) {
    }

    /**
     * Gets the traits for this navigator.
     * @return traits the list of traits, empty by default.
     */
    @NonNull
    public List<SCMTrait<? extends SCMTrait<?>>> getTraits() {
        return Collections.emptyList();
    }

    /**
     * Looks for SCM sources in a configured place.
     * After this method completes, no further calls may be made to the {@code observer} or its child callbacks.
     * <strong>It is vitally important that implementations must periodically call {@link #checkInterrupt()}
     * otherwise it will be impossible for users to interrupt the operation.</strong>
     *
     * @param observer a recipient of progress notifications and a source of contextual information
     * @throws IOException          if scanning fails
     * @throws InterruptedException if scanning is interrupted
     */
    public abstract void visitSources(@NonNull SCMSourceObserver observer) throws IOException, InterruptedException;

    /**
     * Looks for SCM sources in a configured place (scoped against a specific event).
     * After this method completes, no further calls may be made to the {@code observer} or its child callbacks.
     * <strong>It is vitally important that implementations must periodically call {@link #checkInterrupt()}
     * otherwise it will be impossible for users to interrupt the operation.</strong>
     *
     * @param observer a recipient of progress notifications and a source of contextual information
     * @param event    the event from which the operation should be scoped.
     * @throws IOException          if scanning fails
     * @throws InterruptedException if scanning is interrupted
     * @since 2.0
     */
    public void visitSources(@NonNull SCMSourceObserver observer, @NonNull SCMSourceEvent<?> event)
            throws IOException, InterruptedException {
        visitSources(SCMSourceObserver.filter(observer, event.getSourceName()));
    }

    /**
     * Looks for SCM sources in a configured place (scoped against a specific event).
     * After this method completes, no further calls may be made to the {@code observer} or its child callbacks.
     * <strong>It is vitally important that implementations must periodically call {@link #checkInterrupt()}
     * otherwise it will be impossible for users to interrupt the operation.</strong>
     *
     * @param observer a recipient of progress notifications and a source of contextual information
     * @param event    the event from which the operation should be scoped.
     * @throws IOException          if scanning fails
     * @throws InterruptedException if scanning is interrupted
     * @since 2.0
     */
    public void visitSources(@NonNull SCMSourceObserver observer, @NonNull SCMHeadEvent<?> event)
            throws IOException, InterruptedException {
        visitSources(SCMSourceObserver.filter(observer, event.getSourceName()));
    }

    /**
     * Looks for the named SCM source in a configured place.
     * Implementers must ensure that after this method completes, no further calls may be made to the {@code observer}
     * or its child callbacks. Implementations are <strong>strongly encouraged</strong> to override this method.
     *
     * @param sourceName the source to visit.
     * @param observer   a recipient of progress notifications and a source of contextual information
     * @throws IOException          if scanning fails
     * @throws InterruptedException if scanning is interrupted
     * @since 2.0
     */
    public void visitSource(@NonNull String sourceName, @NonNull SCMSourceObserver observer)
            throws IOException, InterruptedException {
        visitSources(SCMSourceObserver.filter(observer, sourceName));
    }

    /**
     * Returns the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports. There will always be
     * exactly one {@link SCMCategory#isUncategorized()} instance in the returned set.
     *
     * @return the set of {@link SCMSourceCategory} that this {@link SCMNavigator} supports.
     * @since 2.0
     */
    @NonNull
    public final Set<? extends SCMSourceCategory> getCategories() {
        Set<? extends SCMSourceCategory> result = getDescriptor().getCategories();
        if (result.size() > 1
                && MethodUtils.isOverridden(SCMNavigator.class,
                getClass(),
                "isCategoryEnabled",
                SCMSourceCategory.class)
                ) {
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
     * @since 2.0
     */
    protected boolean isCategoryEnabled(@NonNull SCMSourceCategory category) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SCMNavigatorDescriptor getDescriptor() {
        return (SCMNavigatorDescriptor) super.getDescriptor();
    }

    /**
     * Get the term used in the UI to represent this kind of {@link SCMNavigator}. Must start with a capital letter.
     *
     * @return the term or {@code null} to fall back to the calling context's default.
     * @since 2.0
     */
    @CheckForNull
    public String getPronoun() {
        return AlternativeUiTextProvider.get(PRONOUN, this, getDescriptor().getPronoun());
    }

    /**
     * Fetches any actions that should be persisted for objects related to the specified owner. For example,
     * if a {@link Item} owns a specific {@link SCMNavigator}, then this method would be called to refresh
     * any {@link Action} instances of that {@link Item}.
     * <p>
     * It is the responsibility of the caller to ensure that these {@link Action} instances are exposed on the
     * {@link Item} for example by providing a {@link TransientActionFactory} implementation that reports these
     * persisted actions separately (for example {@link AbstractProject#getActions()} returns an immutable list,
     * so there is no way to persist the actions from this method against those sub-classes, instead the actions
     * need to be persisted by some side mechanism and then injected into the {@link Actionable#getAllActions()}
     * through a {@link TransientActionFactory} ignoring the cognitive dissonance triggered by adding non-transient
     * actions through a transient action factory... think of it instead as a {@code TemporalActionFactory} that adds
     * actions that can change over time)
     *
     * @param owner    the owner of this {@link SCMNavigator}.
     * @param event    the (optional) event to use when fetching the actions. Where the implementation is
     *                 able to trust the event, it may use the event payload to reduce the number of
     *                 network calls required to obtain the actions.
     * @param listener the listener to report progress on.
     * @return the list of {@link Action} instances to persist.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     * @since 2.0
     */
    @NonNull
    public final List<Action> fetchActions(@NonNull SCMNavigatorOwner owner,
                                           @CheckForNull SCMNavigatorEvent event,
                                           @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        return Util.fixNull(retrieveActions(owner, event, defaultListener(listener)));
    }

    /**
     * SPI for {@link #fetchActions(SCMNavigatorOwner, SCMNavigatorEvent, TaskListener)}. Fetches any actions that
     * should be persisted for objects related to the specified owner.
     *
     * @param owner    the owner of this {@link SCMNavigator}.
     * @param event    the (optional) event to use when fetching the actions. Where the implementation is
     *                 able to trust the event, it may use the event payload to reduce the number of
     *                 network calls required to obtain the actions.
     * @param listener the listener to report progress on.
     * @return the list of {@link Action} instances to persist.
     * @throws IOException          if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     * @since 2.0
     */
    @NonNull
    protected List<Action> retrieveActions(@NonNull SCMNavigatorOwner owner,
                                        @CheckForNull SCMNavigatorEvent event,
                                        @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        return Collections.emptyList();
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

    /**
     * Checks the {@link Thread#interrupted()} and throws an {@link InterruptedException} if it was set.
     *
     * @throws InterruptedException if interrupted.
     * @since 2.0
     */
    protected final void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * Callback from the {@link SCMNavigatorOwner} after the {@link SCMNavigatorOwner} has been saved. Can be used to
     * register the {@link SCMNavigatorOwner} for a call-back hook from the backing SCM that this navigator is for.
     * Implementations are responsible for ensuring that they do not create duplicate registrations and that orphaned
     * registrations are removed eventually.
     *
     * @param owner the {@link SCMNavigatorOwner}.
     * @since 2.0
     */
    public void afterSave(@NonNull SCMNavigatorOwner owner) {
    }

}
