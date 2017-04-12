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
 *
 */

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private static final Set<Class<? extends SCMHeadMixin>> STANDARD_MIXINS =
            Collections.<Class<? extends SCMHeadMixin>>singleton(SCMHeadMixin.class);
    private final List<SCMHeadFilter> filters;

    private final List<SCMSourceCriteria> criteria;

    private final TaskListener listener;

    private final SCMHeadObserver observer;

    private final Set<SCMHead> observerIncludes;

    protected SCMSourceRequest(SCMSourceRequestBuilder<?, ?> builder) {
        this.filters = builder.filters();
        this.criteria = builder.criteria().isEmpty()
                ? Collections.<SCMSourceCriteria>emptyList()
                : Collections.unmodifiableList(new ArrayList<SCMSourceCriteria>(builder.criteria()));
        this.observer = builder.observer();
        this.observerIncludes = observer.getIncludes();
        this.listener = builder.listener();
    }

    public boolean isExcluded(SCMHead head) {
        if (observerIncludes != null && !observerIncludes.contains(head)) {
            return true;
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

    @NonNull
    public List<SCMSourceCriteria> getCriteria() {
        return criteria;
    }

    /**
     * Processes a head in the context of the current request.
     *
     * @param head            the {@link SCMHead} to process.
     * @param revisionFactory factory method that creates the {@link SCMRevision} (assuming creation is cheap).
     * @param probeFactory    factory method that creates the {@link SCMProbe}.
     * @param <H>             the type of {@link SCMHead}.
     * @param <R>             the type of {@link SCMRevision}.
     * @return {@code true} if the {@link SCMHeadObserver} for this request has completed observing, {@code false} to
     * continue processing.
     * @throws IOException          if there was an I/O error.
     * @throws InterruptedException if the processing was interrupted.
     */
    public <H extends SCMHead, R extends SCMRevision> boolean process(final H head,
                                                                      final RevisionFactory<H, R> revisionFactory,
                                                                      ProbeFactory<H, R> probeFactory)
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
        });
    }

    /**
     * Processes a head in the context of the current request where an intermediary operation is required before
     * the {@link SCMRevision} can be instantiated.
     *
     * @param head                the {@link SCMHead} to process.
     * @param intermediateFactory factory method that provides the seed information for both the {@link ProbeFactory}
     *                           and the {@link LazyRevisionFactory}.
     * @param probeFactory        factory method that creates the {@link SCMProbe}.
     * @param revisionFactory     factory method that creates the {@link SCMRevision}.
     * @param <H>                 the type of {@link SCMHead}.
     * @param <I>                 the type of the intermediary operation result.
     * @param <R>                 the type of {@link SCMRevision}.
     * @return {@code true} if the {@link SCMHeadObserver} for this request has completed observing, {@code false} to
     * continue processing.
     * @throws IOException          if there was an I/O error.
     * @throws InterruptedException if the processing was interrupted.
     */
    public <H extends SCMHead, I, R extends SCMRevision> boolean process(H head,
                                                                         IntermediateFactory<I> intermediateFactory,
                                                                         ProbeFactory<H, I> probeFactory,
                                                                         LazyRevisionFactory<H, R, I> revisionFactory)
            throws IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (isExcluded(head)) {
            // not included
            return !observer.isObserving();
        }
        I intermediate = intermediateFactory.create();
        if (!criteria.isEmpty()) {
            SCMSourceCriteria.Probe probe = probeFactory.create(head, intermediate);
            try {
                for (SCMSourceCriteria c : criteria) {
                    if (!c.isHead(probe, listener)) {
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
        observer.observe(head, revisionFactory.create(head, intermediate));
        return !observer.isObserving();
    }

    public boolean isComplete() {
        return !observer.isObserving();
    }

    public TaskListener listener() {
        return listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // default to no-op but allow subclasses to store persistent connections in the request and clean up after
    }

    public interface RevisionFactory<H extends SCMHead, R extends SCMRevision> {
        R create(H head) throws IOException, InterruptedException;
    }

    public interface ProbeFactory<H extends SCMHead, I> {
        SCMSourceCriteria.Probe create(H head, I revision) throws IOException, InterruptedException;
    }

    public interface LazyRevisionFactory<H extends SCMHead, R extends SCMRevision, I> {
        R create(H head, I intermediate) throws IOException, InterruptedException;
    }

    public interface IntermediateFactory<I> {
        I create() throws IOException, InterruptedException;
    }
}
