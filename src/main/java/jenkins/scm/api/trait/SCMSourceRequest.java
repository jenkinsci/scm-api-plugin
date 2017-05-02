/*
 * The MIT License
 *
 * Copyright (c) 2017 CloudBees, Inc.
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

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.mixin.SCMHeadMixin;

/**
 * Represents the context of an individual request for a call to
 * {@link SCMSource#retrieve(SCMSourceCriteria, SCMHeadObserver, SCMHeadEvent, TaskListener)} or an equivalent method.
 *
 * @since 2.2.0
 */
public abstract class SCMSourceRequest implements Closeable {

    /**
     * The {@link SCMSource} to use when applying the {@link #prefilters}.
     */
    @NonNull
    private final SCMSource source;

    /**
     * The filters requiring context of the {@link SCMSourceRequest}, typically because the decision to filter may
     * require making remote requests.
     */
    @NonNull
    private final List<SCMHeadFilter> filters;

    /**
     * The filters that do not require context of the {@link SCMSourceRequest} and only require the {@link SCMSource}
     * and {@link SCMHead} to decide exclusion - typically filtering based on the name or some other attribute of
     * a {@link SCMHeadMixin}.
     */
    @NonNull
    private final List<SCMHeadPrefilter> prefilters;

    /**
     * The authorities that can determine the trustability of a {@link SCMHead}.
     */
    @NonNull
    private final List<SCMHeadAuthority> authorities;

    /**
     * The criteria used to determine if a {@link SCMHead} is discovered by the {@link SCMSource}.
     */
    @NonNull
    private final List<SCMSourceCriteria> criteria;

    /**
     * The {@link TaskListener} active for the scope of the request.
     */
    @NonNull
    private final TaskListener listener;

    /**
     * The {@link SCMHeadObserver} for this request.
     */
    @NonNull
    private final SCMHeadObserver observer;

    /**
     * The {@link SCMHeadObserver#getIncludes()} of {@link #observer}.
     */
    @CheckForNull
    private final Set<SCMHead> observerIncludes;

    /**
     * Any {@link Closeable} objects that should be closed with the request.
     */
    // TODO widen type to AutoClosable once Java 7+
    @NonNull
    private final List<Closeable> managedClosables = new ArrayList<Closeable>();

    /**
     * Constructor.
     *
     * @param source  the source.
     * @param context  the context.
     * @param listener the (optional) {@link TaskListener}.
     */
    protected SCMSourceRequest(@NonNull SCMSource source, @NonNull SCMSourceContext<?, ?> context,
                               @CheckForNull TaskListener listener) {
        this.source = source;
        this.filters = Collections.unmodifiableList(new ArrayList<SCMHeadFilter>(context.filters()));
        this.prefilters = Collections.unmodifiableList(new ArrayList<SCMHeadPrefilter>(context.prefilters()));
        this.authorities = Collections.unmodifiableList(new ArrayList<SCMHeadAuthority>(context.authorities()));
        this.criteria = context.criteria().isEmpty()
                ? Collections.<SCMSourceCriteria>emptyList()
                : Collections.unmodifiableList(new ArrayList<SCMSourceCriteria>(context.criteria()));
        this.observer = context.observer();
        this.observerIncludes = observer.getIncludes();
        this.listener = defaultListener(listener);
    }

    /**
     * Turns a possibly {@code null} {@link TaskListener} reference into a guaranteed non-null reference.
     *
     * @param listener a possibly {@code null} {@link TaskListener} reference.
     * @return guaranteed non-null {@link TaskListener}.
     */
    @NonNull
    private TaskListener defaultListener(@CheckForNull TaskListener listener) {
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
     * Records a processing result to the {@linkplain Witness}es.
     *
     * @param head      the {@link SCMHead}.
     * @param revision  the {@link SCMRevision}.
     * @param isMatch   {@code true} if the head:revision pair was sent to the {@link #observer}.
     * @param witnesses the {@link Witness} instances to notify.
     */
    @SuppressWarnings("unchecked")
    private static void record(@NonNull SCMHead head, SCMRevision revision, boolean isMatch,
                               @NonNull Witness... witnesses) {
        for (Witness witness : witnesses) {
            witness.record(head, revision, isMatch);
        }
    }

    /**
     * Tests if the {@link SCMHead} is excluded from the request.
     *
     * @param head the {@link SCMHead}.
     * @return {@code true} if the {@link SCMHead} is excluded.
     * @throws IOException          if there is an I/O error.
     * @throws InterruptedException if the operation was interrupted.
     */
    public boolean isExcluded(@NonNull SCMHead head) throws IOException, InterruptedException {
        if (observerIncludes != null && !observerIncludes.contains(head)) {
            return true;
        }
        if (!prefilters.isEmpty()) {
            for (SCMHeadPrefilter prefilter : prefilters) {
                if (prefilter.isExcluded(source, head)) {
                    return true;
                }
            }
        }
        if (filters.isEmpty()) {
            return false;
        }
        for (SCMHeadFilter filter : filters) {
            if (filter.isExcluded(this, head)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the {@link SCMHead} is trusted.
     *
     * @param head the {@link SCMHead}.
     * @return {@code true} if the {@link SCMHead} is trusted.
     * @throws IOException          if there is an I/O error.
     * @throws InterruptedException if the operation was interrupted.
     */
    public boolean isTrusted(@NonNull SCMHead head) throws IOException, InterruptedException {
        for (SCMHeadAuthority authority : authorities) {
            if (authority.isTrusted(this, head)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link SCMSourceCriteria} being used for this request.
     *
     * @return the {@link SCMSourceCriteria} being used for this request.
     */
    @NonNull
    public List<SCMSourceCriteria> getCriteria() {
        return criteria;
    }

    /**
     * Processes a head in the context of the current request.
     *
     * @param head         the {@link SCMHead} to process.
     * @param revision     the {@link SCMRevision} (assuming revision creation is very cheap).
     * @param probeFactory factory method that creates the {@link SCMProbe}.
     * @param witnesses    any {@link Witness} instances to be informed of the observation result.
     * @param <H>          the type of {@link SCMHead}.
     * @param <R>          the type of {@link SCMRevision}.
     * @return {@code true} if the {@link SCMHeadObserver} for this request has completed observing, {@code false} to
     * continue processing.
     * @throws IOException          if there was an I/O error.
     * @throws InterruptedException if the processing was interrupted.
     */
    public final <H extends SCMHead, R extends SCMRevision> boolean process(final @NonNull H head,
                                                                            final R revision,
                                                                            @NonNull ProbeFactory<H, R> probeFactory,
                                                                            @NonNull Witness... witnesses)
            throws IOException, InterruptedException {
        return process(head, new IntermediateFactory<R>() {
            @Override
            public R create() throws IOException, InterruptedException {
                return revision;
            }
        }, probeFactory, new LazyRevisionFactory<H, SCMRevision, R>() {
            @Override
            public SCMRevision create(H head, R intermediate) throws IOException, InterruptedException {
                return intermediate;
            }
        }, witnesses);
    }

    /**
     * Processes a head in the context of the current request.
     *
     * @param head            the {@link SCMHead} to process.
     * @param revisionFactory factory method that creates the {@link SCMRevision} (assuming creation is cheap).
     * @param probeFactory    factory method that creates the {@link SCMProbe}.
     * @param witnesses       any {@link Witness} instances to be informed of the observation result.
     * @param <H>             the type of {@link SCMHead}.
     * @param <R>             the type of {@link SCMRevision}.
     * @return {@code true} if the {@link SCMHeadObserver} for this request has completed observing, {@code false} to
     * continue processing.
     * @throws IOException          if there was an I/O error.
     * @throws InterruptedException if the processing was interrupted.
     */
    public final <H extends SCMHead, R extends SCMRevision> boolean process(final @NonNull H head,
                                                                            final @NonNull
                                                                                    RevisionFactory<H, R>
                                                                                    revisionFactory,
                                                                            @NonNull ProbeFactory<H, R> probeFactory,
                                                                            @NonNull Witness... witnesses)
            throws IOException, InterruptedException {
        return process(head, new IntermediateFactory<R>() {
            @Override
            public R create() throws IOException, InterruptedException {
                return revisionFactory.create(head);
            }
        }, probeFactory, new LazyRevisionFactory<H, SCMRevision, R>() {
            @Override
            public SCMRevision create(H head, R intermediate) throws IOException, InterruptedException {
                return intermediate;
            }
        }, witnesses);
    }

    /**
     * Processes a head in the context of the current request where an intermediary operation is required before
     * the {@link SCMRevision} can be instantiated.
     *
     * @param head                the {@link SCMHead} to process.
     * @param intermediateFactory factory method that provides the seed information for both the {@link ProbeFactory}
     *                            and the {@link LazyRevisionFactory}.
     * @param probeFactory        factory method that creates the {@link SCMProbe}.
     * @param revisionFactory     factory method that creates the {@link SCMRevision}.
     * @param witnesses           any {@link Witness} instances to be informed of the observation result.
     * @param <H>                 the type of {@link SCMHead}.
     * @param <I>                 the type of the intermediary operation result.
     * @param <R>                 the type of {@link SCMRevision}.
     * @return {@code true} if the {@link SCMHeadObserver} for this request has completed observing, {@code false} to
     * continue processing.
     * @throws IOException          if there was an I/O error.
     * @throws InterruptedException if the processing was interrupted.
     */
    public final <H extends SCMHead, I, R extends SCMRevision> boolean process(@NonNull H head,
                                                                               @CheckForNull IntermediateFactory<I>
                                                                                       intermediateFactory,
                                                                               @NonNull ProbeFactory<H, I> probeFactory,
                                                                               @NonNull
                                                                                       LazyRevisionFactory<H, R, I>
                                                                                       revisionFactory,
                                                                               @NonNull Witness... witnesses)
            throws IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (isExcluded(head)) {
            // not included
            return !observer.isObserving();
        }
        I intermediate = intermediateFactory == null ? null : intermediateFactory.create();
        if (!criteria.isEmpty()) {
            SCMSourceCriteria.Probe probe = probeFactory.create(head, intermediate);
            try {
                for (SCMSourceCriteria c : criteria) {
                    if (!c.isHead(probe, listener)) {
                        record((H) head, null, false, witnesses);
                        // not a match against criteria
                        return !observer.isObserving();
                    }
                }
            } finally {
                if (probe instanceof Closeable) {
                    ((Closeable) probe).close();
                }
            }
        }
        // observe
        R revision = revisionFactory.create(head, intermediate);
        record(head, revision, true, witnesses);
        observer.observe(head, revision);
        return !observer.isObserving();
    }

    /**
     * Checks if this request has been completed, that is if its {@link SCMHeadObserver} has stopped
     * {@link SCMHeadObserver#isObserving()}.
     *
     * @return {@code true} if and only if the request is completed.
     */
    public boolean isComplete() {
        return !observer.isObserving();
    }

    /**
     * Returns the {@link TaskListener} to use for this request.
     *
     * @return the {@link TaskListener} to use for this request.
     */
    @NonNull
    public TaskListener listener() {
        return listener;
    }

    /**
     * Adds managing a {@link Closeable} into the scope of the {@link SCMSourceRequest}
     *
     * @param closeable the {@link Closeable} to manage.
     */
    public void manage(@CheckForNull Closeable closeable) {
        if (closeable != null) {
            managedClosables.add(closeable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        IOException ioe = null;
        for (Closeable c : managedClosables) {
            try {
                c.close();
            } catch (IOException e) {
                if (ioe == null) {
                    ioe = e;
                } else {
                    // TODO replace with direct call to addSuppressed once baseline Java is 1.7
                    try {
                        Method addSuppressed = Throwable.class.getMethod("addSuppressed", Throwable.class);
                        addSuppressed.invoke(ioe, e);
                    } catch (NoSuchMethodException e1) {
                        // ignore, best effort
                    } catch (IllegalAccessException e1) {
                        // ignore, best effort
                    } catch (InvocationTargetException e1) {
                        // ignore, best effort
                    }
                }
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    public interface RevisionFactory<H extends SCMHead, R extends SCMRevision> {
        @NonNull
        R create(@NonNull H head) throws IOException, InterruptedException;
    }

    public interface ProbeFactory<H extends SCMHead, I> {
        @NonNull
        SCMSourceCriteria.Probe create(@NonNull H head, @Nullable I revision) throws IOException, InterruptedException;
    }

    public interface LazyRevisionFactory<H extends SCMHead, R extends SCMRevision, I> {
        @NonNull
        R create(@NonNull H head, @Nullable I intermediate) throws IOException, InterruptedException;
    }

    public interface IntermediateFactory<I> {
        @Nullable
        I create() throws IOException, InterruptedException;
    }

    public interface Witness<H extends SCMHead, R extends SCMRevision> {
        void record(@NonNull H head, @CheckForNull R revision, boolean isMatch);
    }
}
