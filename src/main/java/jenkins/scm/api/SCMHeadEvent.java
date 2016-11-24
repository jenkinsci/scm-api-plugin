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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Base class for events relating to {@link SCMHead} instances.
 *
 * @param <P> the (provider specific) payload.
 * @since FIXME
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
     *
     * @return the name of the {@link SCMSource}
     */
    @NonNull
    @Untrusted
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
    @Untrusted
    public abstract Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source);

    /**
     * Tests if this event applies to the supplied {@link SCM}.
     *
     * @param scm the {@link SCM}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCM}.
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

    private class Validated<O extends SCMHeadObserver> extends SCMHeadObserver.Wrapped<O> {
        private final Set<SCMHead> includes;
        private final Map<SCMHead, SCMRevision> untrusted;
        private final Map<SCMHead, SCMRevision> trusted;

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

        @Override
        public void observe(@NonNull SCMHead head, @NonNull SCMRevision revision) {
            if (untrusted.containsKey(head)) {
                trusted.put(head, revision);
                untrusted.remove(head);
                super.observe(head, revision);
            }
        }

        @Override
        public boolean isObserving() {
            return !untrusted.isEmpty() && super.isObserving();
        }

        public Map<SCMHead, SCMRevision> heads() {
            return trusted;
        }

        @Override
        public Set<SCMHead> getIncludes() {
            return includes;
        }

    }
}
