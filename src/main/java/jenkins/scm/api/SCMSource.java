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
     * @param observer an optional observer of interim results.
     * @param listener the task listener
     * @return the provided observer.
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
     * @throws IOException
     */
    @NonNull
    public final Set<SCMHead> fetch(@CheckForNull TaskListener listener) throws IOException, InterruptedException {
        return retrieve(defaultListener(listener));
    }

    /**
     * Fetches the current list of heads. Implementers are free to cache intermediary results
     * but the call must always check the validity of any intermediary caches.
     *
     * @param listener the task listener
     * @return the current list of heads.
     * @throws IOException
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
     * @throws IOException
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
     * @throws IOException
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
