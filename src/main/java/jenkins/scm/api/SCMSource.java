/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
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
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.LogTaskListener;
import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link SCMSource} is responsible for fetching {@link SCMHead} and corresponding {@link SCMRevision} instances from
 * which it can build {@link SCM} instances that are configured to check out the specific {@link SCMHead} at the
 * specified {@link SCMRevision}.
 *
 * Each {@link SCMSource} is owned by a {@link SCMSourceOwner}, if you need to find all the owners use
 * {@link SCMSourceOwners#all()} to iterate through them, e.g. to notify {@link SCMSource} instances of push
 * notification from the server they source {@link SCMHead}s from.
 *
 * <strong>NOTE:</strong> This layer does not cache remote calls but can cache intermediary results. For example,
 * with Subversion it is acceptable to cache the last revisions of various directory entries to minimize network
 * round trips, but any of the calls to {@link #fetch(TaskListener)},
 * {@link #fetch(SCMHeadObserver, hudson.model.TaskListener)} or
 * {@link #fetch(SCMHead, hudson.model.TaskListener)} must
 * involve at least one network round trip to validate any cached information.
 */
public abstract class SCMSource extends AbstractDescribableImpl<SCMSource>
        implements ExtensionPoint {

    /**
     * The ID of this source.
     */
    @CheckForNull
    @GuardedBy("this")
    private String id;

    /**
     * Constructor.
     *
     * @param id the id or {@code null}.
     */
    protected SCMSource(@CheckForNull String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
    }

    /**
     * The ID of this source. The ID is not related to anything at all.
     *
     * @return the ID of this source.
     */
    @NonNull
    public final synchronized String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    /**
     * The owner of this source, used as a context for looking up things such as credentials.
     */
    @GuardedBy("this")
    @CheckForNull
    private transient SCMSourceOwner owner;

    /**
     * Sets the owner.
     *
     * @param owner the owner.
     */
    public final synchronized void setOwner(@CheckForNull SCMSourceOwner owner) {
        this.owner = owner;
    }

    /**
     * Gets the owner.
     *
     * @return the owner.
     */
    @CheckForNull
    public final synchronized SCMSourceOwner getOwner() {
        return owner;
    }

    /**
     * Returns the branch criteria.
     *
     * @return the branch criteria.
     */
    @CheckForNull
    protected final SCMSourceCriteria getCriteria() {
        final SCMSourceOwner owner = getOwner();
        if (owner == null) {
            return null;
        }
        return owner.getSCMSourceCriteria(this);
    }

    /**
     * Fetches the latest heads and corresponding revisions. Implementers are free to cache intermediary results
     * but the call must always check the validity of any intermediary caches.
     *
     * @param <O> Observer type.
     * @param observer an optional observer of interim results.
     * @param listener the task listener
     * @return the provided observer.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    public final <O extends SCMHeadObserver> O fetch(@NonNull O observer,
                                                        @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        retrieve(observer, defaultListener(listener));
        return observer;
    }

    /**
     * Fetches the latest heads and corresponding revisions. Implementers are free to cache intermediary results
     * but the call must always check the validity of any intermediary caches.
     *
     * @param observer an optional observer of interim results.
     * @param listener the task listener.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    protected abstract void retrieve(@NonNull SCMHeadObserver observer,
                                     @NonNull TaskListener listener)
            throws IOException, InterruptedException;

    /**
     * Fetches the current list of heads. Implementers are free to cache intermediary results
     * but the call must always check the validity of any intermediary caches.
     *
     * @param listener the task listener
     * @return the current list of heads.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    public final Set<SCMHead> fetch(@CheckForNull TaskListener listener) throws IOException, InterruptedException {
        return retrieve(defaultListener(listener));
    }

    /**
     * Looks up the immediate parent revision(s) of the specified revision within the specified head.
     *
     * @param head     the head to look up the parent revision(s) within.
     * @param revision the revision to lookup the immediate parent(s) of.
     * @param listener the task listener.
     * @return a set of immediate parent revisions of the specified revision. An empty set indicates either that the
     *         parents are unknown or that the revision is a root revision. Where the backing SCM supports merge
     *         tracking there is the potential for multiple parent revisions reflecting that the specified revision
     *         was a merge of more than one revision and thus has more than one parent.
     * @since 0.3
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    public Set<SCMRevision> parentRevisions(@NonNull SCMHead head, @NonNull SCMRevision revision,
                                            @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        return Collections.emptySet();
    }

    /**
     * Looks up the immediate parent heads of the specified head within the specified source.
     *
     * @param head     the head to look up the parent head(s) within.
     * @param listener the task listener.
     * @return a map of immediate parent heads of the specified head where the heads are the keys and the revisions
     *         at which the parent relationship was established are the values. An empty map indicates either that the
     *         parents are unknown or that the head is a root head. Where the backing SCM supports merge
     *         tracking there is the potential for multiple parent heads reflecting that the specified head
     *         was a merge of more than one head and thus has more than one parent.
     * @since 0.3
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    public Map<SCMHead, SCMRevision> parentHeads(@NonNull SCMHead head, @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        return Collections.emptyMap();
    }

    /**
     * Fetches the current list of heads. Implementers are free to cache intermediary results
     * but the call must always check the validity of any intermediary caches.
     *
     * @param listener the task listener
     * @return the current list of heads.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @NonNull
    protected Set<SCMHead> retrieve(@NonNull TaskListener listener) throws IOException, InterruptedException {
        return fetch(SCMHeadObserver.collect(), listener).result().keySet();
    }

    /**
     * Gets the current head revision of the specified head.
     *
     * @param head     the head.
     * @param listener the task listener
     * @return the revision hash (may be non-deterministic) or {@code null} if the head no longer exists.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @CheckForNull
    public final SCMRevision fetch(@NonNull SCMHead head, @CheckForNull TaskListener listener)
            throws IOException, InterruptedException {
        return retrieve(head, defaultListener(listener));
    }

    /**
     * Gets the current head revision of the specified head.
     *
     * @param head     the head.
     * @param listener the task listener
     * @return the revision hash (may be non-deterministic) or {@code null} if the head no longer exists.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    @CheckForNull
    protected SCMRevision retrieve(@NonNull SCMHead head, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        return fetch(SCMHeadObserver.select(head), listener).result();
    }

    /**
     * Builds a {@link SCM} instance for the specified head and revision, no validation of the
     * head is performed, a revision for a different head or source will be treated as equivalent to a
     * {@code null} revision.
     *
     * @param head     the head.
     * @param revision the revision or {@code null}.
     * @return the {@link SCM} instance.
     */
    @NonNull
    public abstract SCM build(@NonNull SCMHead head, @CheckForNull SCMRevision revision);

    /**
     * Builds a {@link SCM} instance for the specified head.
     *
     * @param head the head.
     * @return the {@link SCM} instance
     */
    @NonNull
    public final SCM build(@NonNull SCMHead head) {
        return build(head, null);
    }

    /**
     * Enables a source to request that an alternative revision be used to obtain security-sensitive build instructions.
     * <p>Normally it is assumed that revisions in the SCM represented by this source
     * come from principals operating with the same authorization as the principal creating the job,
     * or at least with authorization to create a similar job.
     * <p>A source may however collect revisions from untrusted third parties and submit them for builds.
     * If the project type performing the build loads instructions from the same revision,
     * this might allow the job to be subverted to perform unauthorized build steps or steal credentials.
     * <p>By replacing the supplied revision with a trusted variant, a source can defend against such attacks.
     * It is up to the project type to determine which files should come from a trusted replacement.
     * Regular project sources should come from the original;
     * Jenkins-specific scripting commands or configuration should come from the replacement, unless easily sandboxed;
     * scripts for external build tools should come from the original if possible.
     * @param revision a revision (produced by one of the {@code retrieve} overloads)
     *                 which may or may not come from a trustworthy source
     * @param listener a way to explain possible substitutions
     * @return by default, {@code revision};
     *         may be overridden to provide an alternate revision from the same or a different head
     * @throws IOException in case the implementation must call {@link #fetch(SCMHead, TaskListener)} or similar
     * @throws InterruptedException in case the implementation must call {@link #fetch(SCMHead, TaskListener)} or similar
     * @since FIXME
     */
    @NonNull
    public SCMRevision getTrustedRevision(@NonNull SCMRevision revision, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        return revision;
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
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SCMSource)) {
            return false;
        }

        SCMSource that = (SCMSource) o;

        return getId().equals(that.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("{id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
