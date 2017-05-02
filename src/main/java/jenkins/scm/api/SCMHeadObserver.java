/*
 * The MIT License
 *
 * Copyright (c) 2011-2017, CloudBees, Inc., Stephen Connolly.
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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.jcip.annotations.GuardedBy;

/**
 * Something that observes {@link SCMHead} and corresponding {@link SCMRevision} details.
 *
 * @author Stephen Connolly
 */
public abstract class SCMHeadObserver {

    /**
     * Observes a head and current revision.
     *
     * @param head     the head.
     * @param revision the revision.
     * @throws IOException if processing of the observation could not be completed due to an {@link IOException}.
     * @throws InterruptedException  if processing of the observation was interrupted
     */
    public abstract void observe(@NonNull SCMHead head, @NonNull SCMRevision revision)
            throws IOException, InterruptedException;

    /**
     * Returns information about whether the observer wants more results.
     *
     * @return {@code true} if the observer is still observing or {@code false} to signal that it is ok to stop early.
     */
    public boolean isObserving() {
        return true;
    }

    /**
     * Returns the subset of {@link SCMHead} instances that this observer is interested in or {@code null} if
     * interested in all {@link SCMHead} instances.
     * <p>
     * <strong>Implementations should not assume that the {@link #getIncludes()} will be honoured.</strong>
     * This method is designed to provide a <i>hint</i> to {@link SCMSource} implementations.
     *
     * @return the subset of {@link SCMHead} instances that this observer is interested in or {@code null}.
     * @since 2.0
     */
    @CheckForNull
    public Set<SCMHead> getIncludes() {
        return null;
    }

    /**
     * Wraps multiple observers returning a combined observer that remains observing as long as at least one of the
     * wrapped observers
     * is still observing.
     *
     * @param observers the observers to wrap.
     * @return a wrapped observer.
     */
    @NonNull
    public static AllFinished allOf(SCMHeadObserver... observers) {
        return new AllFinished(observers);
    }

    /**
     * Wraps multiple observers returning a combined observer that remains observing until one of the wrapped observers
     * stops observing.
     *
     * @param observers the observers to wrap.
     * @return a wrapped observer.
     */
    @NonNull
    public static OneFinished first(SCMHeadObserver... observers) {
        return new OneFinished(observers);
    }

    /**
     * Creates an observer that collects all the heads and revisions.
     *
     * @return an observer that collects all the heads and revisions.
     */
    @NonNull
    public static Collector collect() {
        return new Collector();
    }

    /**
     * Creates an observer that selects the revision of a specific head.
     *
     * @param head the head to watch out for.
     * @return an observer that selects the revision of a specific head.
     */
    @NonNull
    public static Selector select(@NonNull SCMHead head) {
        return new Selector(head);
    }

    /**
     * Creates an observer that filters a delegates observer to the specified heads
     *
     * @param <O>      the type of observer that will be filtered.
     * @param delegate the delegate
     * @param heads    the head to watch out for.
     * @return an observer that wraps the supplied delegate.
     */
    @NonNull
    public static <O extends SCMHeadObserver> Filter<O> filter(O delegate, SCMHead... heads) {
        return new Filter<O>(delegate, heads);
    }

    /**
     * Creates an observer that selects the revision of a specific head.
     *
     * @param headName the head to watch out for.
     * @return an observer that selects the revision of a specific head.
     * @since 2.0
     */
    @NonNull
    public static Named named(@NonNull String headName) {
        return new Named(headName);
    }

    /**
     * Creates an observer that selects the first revision it finds. Also useful for quick checks of non-empty.
     *
     * @return an observer that selects the first revision of a any head.
     * @since 2.0
     */
    @NonNull
    public static Any any() {
        return new Any();
    }

    /**
     * Creates an observer that selects the first revision it finds. Also useful for quick checks of non-empty.
     *
     * @return an observer that selects the first revision of a any head.
     * @since 2.2.0
     */
    @NonNull
    public static None none() {
        return None.INSTANCE;
    }

    /**
     * An observer that wraps multiple observers and keeps observing as long as one of the wrapped observers wants to.
     */
    public static class AllFinished extends SCMHeadObserver {
        /**
         * Our {@link #getIncludes()}
         */
        @GuardedBy("this")
        private Set<SCMHead> includes = null;
        /**
         * Flag to track initialization of {@link #includes}
         */
        @GuardedBy("this")
        private boolean includesPopulated;
        /**
         * The wrapped observers.
         */
        @NonNull
        private final Iterable<SCMHeadObserver> observers;

        /**
         * Constructor.
         *
         * @param observers the observers to wrap.
         */
        public AllFinished(SCMHeadObserver... observers) {
            this(Arrays.asList(observers));
        }

        /**
         * Constructor.
         *
         * @param observers the observers to wrap.
         */
        public AllFinished(@NonNull Iterable<SCMHeadObserver> observers) {
            observers.getClass(); // fail fast if null
            this.observers = observers;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision)
                throws IOException, InterruptedException {
            for (SCMHeadObserver observer : observers) {
                if (observer.isObserving()) {
                    observer.observe(head, revision);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            for (SCMHeadObserver observer : observers) {
                if (observer.isObserving()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Set<SCMHead> getIncludes() {
            if (includesPopulated) {
                return includes;
            }
            Set<SCMHead> result = null;
            for (SCMHeadObserver observer : observers) {
                Set<SCMHead> includes = observer.getIncludes();
                if (includes == null) {
                    // at least one of the observers is interested in everything, thus we are also
                    this.includes = null;
                    includesPopulated = true;
                    return null;
                }
                if (result == null) {
                    result = new HashSet<SCMHead>(includes);
                } else {
                    result.addAll(includes);
                }
            }
            includes = result;
            includesPopulated = true;
            return result;
        }
    }

    /**
     * An observer that wraps multiple observers and keeps observing until one of the wrapped observers stops observing.
     */
    public static class OneFinished extends SCMHeadObserver {
        /**
         * Our {@link #getIncludes()}
         */
        @GuardedBy("this")
        private Set<SCMHead> includes = null;
        /**
         * Flag to track initialization of {@link #includes}
         */
        @GuardedBy("this")
        private boolean includesPopulated;
        /**
         * The wrapped observers.
         */
        @NonNull
        private final Iterable<SCMHeadObserver> observers;

        /**
         * Constructor.
         *
         * @param observers the observers to wrap.
         */
        public OneFinished(SCMHeadObserver... observers) {
            this(Arrays.asList(observers));
        }

        /**
         * Constructor.
         *
         * @param observers the observers to wrap.
         */
        public OneFinished(@NonNull Iterable<SCMHeadObserver> observers) {
            observers.getClass(); // fail fast if null
            this.observers = observers;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision)
                throws IOException, InterruptedException {
            for (SCMHeadObserver observer : observers) {
                if (observer.isObserving()) {
                    observer.observe(head, revision);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            for (SCMHeadObserver observer : observers) {
                if (!observer.isObserving()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Set<SCMHead> getIncludes() {
            if (includesPopulated) {
                return includes;
            }
            Set<SCMHead> result = null;
            for (SCMHeadObserver observer : observers) {
                Set<SCMHead> includes = observer.getIncludes();
                if (includes == null) {
                    // at least one of the observers is interested in everything, thus we are also
                    this.includes = null;
                    includesPopulated = true;
                    return null;
                }
                if (result == null) {
                    result = new HashSet<SCMHead>(includes);
                } else {
                    result.addAll(includes);
                }
            }
            includes = result;
            includesPopulated = true;
            return result;
        }
    }

    /**
     * An observer that collects the observed {@link SCMHead}s and {@link SCMRevision}s.
     */
    public static class Collector extends SCMHeadObserver {
        /**
         * The collected results.
         */
        @NonNull
        private final Map<SCMHead, SCMRevision> result = new TreeMap<SCMHead, SCMRevision>();

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            result.put(head, revision);
        }

        /**
         * Returns the collected results.
         *
         * @return the collected results.
         */
        @NonNull
        public Map<SCMHead, SCMRevision> result() {
            return result;
        }
    }

    /**
     * An observer that collects the {@link SCMRevision} of a specific {@link SCMHead} and then stops observing.
     */
    public static class Selector extends SCMHeadObserver {
        /**
         * The {@link SCMHead} we are waiting for.
         */
        @NonNull
        private final SCMHead head;
        /**
         * The corresponding {@link SCMRevision}.
         */
        @CheckForNull
        private SCMRevision revision;

        /**
         * Constructor.
         *
         * @param head the {@link SCMHead} to get the {@link SCMRevision} of.
         */
        public Selector(@NonNull SCMHead head) {
            head.getClass(); // fail fast if null
            this.head = head;
        }

        /**
         * Returns the result.
         *
         * @return the result.
         */
        @CheckForNull
        public SCMRevision result() {
            return revision;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            if (this.head.equals(head)) {
                this.revision = revision;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return revision == null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<SCMHead> getIncludes() {
            return Collections.singleton(head);
        }
    }

    /**
     * An observer that collects the {@link SCMRevision} of a named {@link SCMHead} and then stops observing.
     */
    public static class Named extends SCMHeadObserver {
        /**
         * The {@link SCMHead#getName()} we are waiting for.
         */
        @NonNull
        private final String head;
        /**
         * The corresponding {@link SCMRevision}.
         */
        @CheckForNull
        private SCMRevision revision;

        /**
         * Constructor.
         *
         * @param head the {@link SCMHead#getName()} to get the {@link SCMRevision} of.
         */
        public Named(@NonNull String head) {
            head.getClass(); // fail fast if null
            this.head = head;
        }

        /**
         * Returns the result.
         *
         * @return the result.
         */
        @CheckForNull
        public SCMRevision result() {
            return revision;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            if (this.head.equals(head.getName())) {
                this.revision = revision;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return revision == null;
        }

    }

    /**
     * An observer that picks the first revision it can find.
     */
    public static class Any extends SCMHeadObserver {
        /**
         * Any {@link SCMRevision}.
         */
        @CheckForNull
        private SCMRevision revision;

        /**
         * Constructor.
         */
        public Any() {
        }

        /**
         * Returns the result.
         *
         * @return the result.
         */
        public SCMRevision getRevision() {
            return revision;
        }

        /**
         * Returns the result.
         *
         * @return the result.
         */
        @CheckForNull
        public SCMRevision result() {
            return revision;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            this.revision = revision;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return revision == null;
        }

    }

    /**
     * An observer that is already finished.
     *
     * @since 2.2.0
     */
    public static final class None extends SCMHeadObserver {
        /**
         * Singleton.
         */
        public static final None INSTANCE = new None();

        /**
         * Constructor.
         */
        private None() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return false;
        }
    }

    /**
     * Base class for an {@link SCMHeadObserver} that wraps another {@link SCMHeadObserver} while allowing access to the
     * original observer via {@link #unwrap()}.
     *
     * @param <O> the type of wrapped {@link SCMHeadObserver}
     * @since 2.0
     */
    public static abstract class Wrapped<O extends SCMHeadObserver> extends SCMHeadObserver {
        /**
         * The wrapped {@link SCMHeadObserver}
         */
        private final O delegate;

        /**
         * Constructor.
         *
         * @param delegate the {@link SCMHeadObserver} to wrap.
         */
        protected Wrapped(O delegate) {
            this.delegate = delegate;
        }

        /**
         * Unwraps this {@link SCMHeadObserver}.
         *
         * @return the wrapped {@link SCMHeadObserver}.
         */
        public O unwrap() {
            return delegate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return delegate.isObserving();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision)
                throws IOException, InterruptedException {
            delegate.observe(head, revision);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<SCMHead> getIncludes() {
            return delegate.getIncludes();
        }
    }

    /**
     * A {@link SCMHeadObserver} that filters the {@link SCMHead} instances that will be observed by the wrapped
     * {@link SCMHeadObserver}.
     *
     * @param <O> the type of wrapped {@link SCMHeadObserver}
     * @since 2.0
     */
    public static class Filter<O extends SCMHeadObserver> extends Wrapped<O> {
        /**
         * The {@link SCMHead} instances that we are including.
         */
        private final Set<SCMHead> heads;
        /**
         * The {@link SCMHead} instances we have yet to observe.
         */
        private final Set<SCMHead> remaining;

        /**
         * Constructor.
         *
         * @param delegate The {@link SCMHeadObserver} to wrap.
         * @param heads    The {@link SCMHead} instances that my be observed by the wrapped {@link SCMHeadObserver}.
         */
        public Filter(O delegate, SCMHead... heads) {
            super(delegate);
            this.heads = new HashSet<SCMHead>(Arrays.asList(heads));
            Set<SCMHead> includes = super.getIncludes();
            if (includes != null) {
                this.heads.retainAll(includes);
            }
            this.remaining = new HashSet<SCMHead>(this.heads);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision)
                throws IOException, InterruptedException {
            if (remaining.contains(head)) {
                remaining.remove(head);
                super.observe(head, revision);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return !remaining.isEmpty() && super.isObserving();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<SCMHead> getIncludes() {
            return heads;
        }

    }

}
