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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Base class for events relating to {@link SCMNavigator} instances.
 *
 * @param <P> the (provider specific) payload.
 * @since 2.0
 */
public abstract class SCMNavigatorEvent<P> extends SCMEvent<P> {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SCMHeadEvent.class.getName());

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public SCMNavigatorEvent(@NonNull Type type, long timestamp, @NonNull P payload) {
        super(type, timestamp, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMNavigatorEvent(@NonNull Type type, long timestamp, @NonNull P payload, @CheckForNull String origin) {
        super(type, timestamp, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public SCMNavigatorEvent(@NonNull Type type, @NonNull P payload) {
        super(type, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMNavigatorEvent(@NonNull Type type, @NonNull P payload, @CheckForNull String origin) {
        super(type, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    protected SCMNavigatorEvent(@NonNull SCMNavigatorEvent<P> src) {
        super(src);
    }

    /**
     * Tests if this event applies to the supplied {@link SCMNavigator}.
     *
     * @param navigator the {@link SCMNavigator}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCMNavigator}.
     */
    public abstract boolean isMatch(SCMNavigator navigator);

    /**
     * Return a description of the event in the context of the supplied {@link SCMNavigator}.
     *
     * @param navigator the {@link SCMNavigator}, the navigator must be {@link #isMatch(SCMNavigator)}.
     * @return the description or {@code null} if no description can be provided.
     * @since TODO
     */
    @CheckForNull
    public String descriptionFor(SCMNavigator navigator) {
        return description();
    }

    /**
     * Fires the {@link SCMNavigatorEvent} to all registered {@link SCMEventListener} instances.
     *
     * @param event the event to fire.
     */
    public static void fireNow(@NonNull final SCMNavigatorEvent<?> event) {
        executorService().execute(new DispatcherImpl(event));
    }

    /**
     * Fires the {@link SCMNavigatorEvent} to all registered {@link SCMEventListener} instances after the specified delay.
     *
     * @param event      the event to fire.
     * @param delay      how long to wait before firing the event.
     * @param delayUnits the units of time in which the delay is expressed.
     */
    public static void fireLater(@NonNull final SCMNavigatorEvent<?> event, long delay, TimeUnit delayUnits) {
        executorService().schedule(new DispatcherImpl(event), delay, delayUnits);
    }

    private static class DispatcherImpl extends SCMEvent.Dispatcher<SCMNavigatorEvent<?>> {
        private DispatcherImpl(SCMNavigatorEvent<?> event) {
            super(event);
        }

        @Override
        protected void log(SCMEventListener l, Throwable e) {
            LogRecord lr = new LogRecord(Level.WARNING,
                    "SCMEventListener.onSCMSourceEvent(SCMSourceEvent) {0} propagated an exception"
            );
            lr.setThrown(e);
            lr.setParameters(new Object[]{l});
            LOGGER.log(lr);
        }

        @Override
        protected void fire(SCMEventListener l, SCMNavigatorEvent<?> event) {
            l.onSCMNavigatorEvent(event);
        }
    }
}
