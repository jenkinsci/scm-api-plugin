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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

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
     */
    public abstract void observe(@NonNull SCMHead head, @NonNull SCMRevision revision);

    /**
     * Returns information about whether the observer wants more results.
     *
     * @return {@code true} if the observer is still observing or {@code false} to signal that it is ok to stop early.
     */
    public boolean isObserving() {
        return true;
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
     * Creates an observer that selects the revision of a specific head.
     *
     * @param headName the head to watch out for.
     * @return an observer that selects the revision of a specific head.
     * @since FIXME
     */
    @NonNull
    public static Named named(@NonNull String headName) {
        return new Named(headName);
    }

    /**
     * Creates an observer that selects the first revision it finds. Also useful for quick checks of non-empty.
     *
     * @return an observer that selects the first revision of a any head.
     * @since FIXME
     */
    @NonNull
    public static Any any() {
        return new Any();
    }

    /**
     * An observer that wraps multiple observers and keeps observing as long as one of the wrapped observers wants to.
     */
    public static class AllFinished extends SCMHeadObserver {
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
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            for (SCMHeadObserver observer : observers) {
                observer.observe(head, revision);
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
    }

    /**
     * An observer that wraps multiple observers and keeps observing until one of the wrapped observers stops observing.
     */
    public static class OneFinished extends SCMHeadObserver {
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
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            for (SCMHeadObserver observer : observers) {
                observer.observe(head, revision);
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

        public SCMRevision getRevision() {
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

}
