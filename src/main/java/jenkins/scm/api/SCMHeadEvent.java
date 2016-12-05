/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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

package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jenkins.scm.impl.SCMTriggerListener;

/**
 * Base class for events relating to {@link SCMHead} instances.
 *
 * @param <P> the (provider specific) payload.
 * @since 2.0
 */
public abstract class SCMHeadEvent<P> extends SCMEvent<P> {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SCMHeadEvent.class.getName());

    /**
     * {@inheritDoc}
     */
    public SCMHeadEvent(@NonNull Type type, long timestamp, @NonNull P payload) {
        super(type, timestamp, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMHeadEvent(@NonNull Type type, @NonNull P payload) {
        super(type, payload);
    }

    /**
     * {@inheritDoc}
     */
    protected SCMHeadEvent(@NonNull SCMHeadEvent<P> src) {
        super(src);
    }

    /**
     * Tests if this event applies to the supplied {@link SCMNavigator}.
     *
     * @param navigator the {@link SCMNavigator}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCMNavigator}.
     */
    public abstract boolean isMatch(@NonNull SCMNavigator navigator);

    /**
     * Returns the name of the {@link SCMSource}, such as a repository name within an organization; may be used as an
     * {@link Item#getName}. Must be the same as the name that would be passed to
     * {@link SCMSourceObserver#observe(String)} by any {@link SCMNavigator} that {@link #isMatch(SCMNavigator)}.
     * <p>
     * <strong>NOTE:</strong> if and only if {@link #isMatch(SCMNavigator)} <i>always</i> returns {@code false} then
     * the value returned here does not matter and a dummy value can be returned instead.
     * <p>
     * <strong>DO NOT TRUST THE RETURN VALUES.</strong> Data from events should only be used as a rumour that requires
     * verification.
     *
     * @return the name of the {@link SCMSource}
     */
    @NonNull
    public abstract String getSourceName();

    /**
     * Tests if this event applies to the supplied {@link SCMSource}.
     *
     * @param source the {@link SCMSource}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCMSource}.
     */
    public boolean isMatch(@NonNull SCMSource source) {
        return !heads(source).isEmpty();
    }

    /**
     * Returns the {@link SCMHead} for the supplied {@link SCMSource} that this event corresponds to.
     * <p>
     * <strong>DO NOT TRUST THE RETURN VALUES.</strong> Data from events should only be used as a rumour that requires
     * verification.
     *
     * @param source the {@link SCMSource}.
     * @return the {@link SCMHead} (and optional {@link SCMRevision}) that this event corresponds to when considered
     * from the point of view of the supplied {@link SCMSource}. The map may be empty in the case where the event is
     * not relevant to the supplied {@link SCMSource}
     */
    @NonNull
    public abstract Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source);

    /**
     * Tests if this event applies to the supplied {@link SCM}. Implementations that return {@code true} will trigger
     * polling for the matching jobs that have enabled the {@link SCMTrigger} and have not disabled the post commit
     * hooks {@link SCMTrigger#isIgnorePostCommitHooks()}.
     * <p>
     * <strong>NOTE:</strong> if you are implementing {@link SCMHeadEvent} and you already have a separate code path
     * responsible for notifying {@link SCMTrigger} then you should <strong>either</strong> remove that code path
     * <strong>or</strong> always return {@code false} from this method. The recommendation is to consolidate on
     * {@link SCMHeadEvent} based triggering as that minimizes the number of times the graph of all items needs to
     * be traversed by event listeners.
     *
     * @param scm the {@link SCM}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCM}.
     * @see SCMTriggerListener
     * @see SCMTrigger#isIgnorePostCommitHooks()
     */
    public abstract boolean isMatch(@NonNull SCM scm);

    /**
     * Wraps a {@link SCMHeadObserver} such that the wrapped observer will only observe {@link SCMHead} instances
     * mentioned in this event.
     *
     * @param source   the {@link SCMSource}.
     * @param delegate the delegate.
     * @param <O>      the type of delegate.
     * @return the wrapped delegate.
     */
    public <O extends SCMHeadObserver> SCMHeadObserver.Wrapped<O> filter(@NonNull SCMSource source, O delegate) {
        return new Validated<O>(delegate, source);
    }

    /**
     * Fires the {@link SCMHeadEvent} to all registered {@link SCMEventListener} instances.
     *
     * @param event the event to fire.
     */
    public static void fireNow(@NonNull final SCMHeadEvent<?> event) {
        executorService().execute(new Dispatcher(event));
    }

    /**
     * Fires the {@link SCMHeadEvent} to all registered {@link SCMEventListener} instances after the specified delay.
     *
     * @param event      the event to fire.
     * @param delay      how long to wait before firing the event.
     * @param delayUnits the units of time in which the delay is expressed.
     */
    public static void fireLater(@NonNull final SCMHeadEvent<?> event, long delay, TimeUnit delayUnits) {
        executorService().schedule(new Dispatcher(event), delay, delayUnits);
    }

    private static class Dispatcher implements Runnable {
        private final SCMHeadEvent<?> event;

        public Dispatcher(SCMHeadEvent<?> event) {
            this.event = event;
        }

        @Override
        public void run() {
            String oldName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(String.format("SCMHeadEvent %tc / %s",
                        event.getTimestamp(), oldName)
                );
                for (final SCMEventListener l : ExtensionList.lookup(SCMEventListener.class)) {
                    try {
                        l.onSCMHeadEvent(event);
                    } catch (LinkageError e) {
                        LogRecord lr = new LogRecord(Level.WARNING,
                                "SCMEventListener.onSCMHeadEvent(SCMHeadEvent) {0} propagated an exception"
                        );
                        lr.setThrown(e);
                        lr.setParameters(new Object[]{l});
                        LOGGER.log(lr);
                    } catch (Error e) {
                        throw e;
                    } catch (Throwable e) {
                        LogRecord lr = new LogRecord(Level.WARNING,
                                "SCMEventListener.onSCMHeadEvent(SCMHeadEvent) {0} propagated an exception"
                        );
                        lr.setThrown(e);
                        lr.setParameters(new Object[]{l});
                        LOGGER.log(lr);
                    }
                }
            } finally {
                Thread.currentThread().setName(oldName);
            }
        }
    }

    /**
     * This {@link SCMHeadObserver} wraps a delegate {@link SCMHeadObserver} such that only those {@link SCMHead}
     * instances that are both mentioned in the event and actually available from the source are observed by the
     * delegate.
     * @param <O> the delegate observer class.
     */
    private class Validated<O extends SCMHeadObserver> extends SCMHeadObserver.Wrapped<O> {
        /**
         * The {@link SCMHead} instances we are interested in.
         */
        private final Set<SCMHead> includes;
        /**
         * The heads and revisions mentioned in the event.
         */
        private final Map<SCMHead, SCMRevision> untrusted;
        /**
         * The actual heads and revisions confirmed by the source.
         */
        private final Map<SCMHead, SCMRevision> trusted;

        /**
         * Constructor.
         * @param delegate the delegate.
         * @param source the source to validate against.
         */
        private Validated(O delegate, SCMSource source) {
            super(delegate);
            untrusted = new HashMap<SCMHead, SCMRevision>(SCMHeadEvent.this.heads(source));
            Set<SCMHead> i = super.getIncludes();
            if (i != null) {
                untrusted.keySet().retainAll(i);
            }
            includes = new HashSet<SCMHead>(untrusted.keySet()); // copy now because we use untrusted to track progress
            trusted = new HashMap<SCMHead, SCMRevision>(untrusted.size());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            if (untrusted.containsKey(head)) {
                trusted.put(head, revision);
                untrusted.remove(head);
                super.observe(head, revision);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isObserving() {
            return !untrusted.isEmpty() && super.isObserving();
        }

        public Map<SCMHead, SCMRevision> heads() {
            return trusted;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<SCMHead> getIncludes() {
            return includes;
        }

    }
}
