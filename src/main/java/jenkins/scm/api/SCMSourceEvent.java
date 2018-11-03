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
import hudson.model.Item;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Base class for events relating to {@link SCMSource} instances.
 *
 * @param <P> the (provider specific) payload.
 * @since 2.0
 */
public abstract class SCMSourceEvent<P> extends SCMEvent<P> {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SCMSourceEvent.class.getName());

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public SCMSourceEvent(@NonNull Type type, long timestamp, @NonNull P payload) {
        super(type, timestamp, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMSourceEvent(@NonNull Type type, long timestamp, @NonNull P payload, @CheckForNull String origin) {
        super(type, timestamp, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public SCMSourceEvent(@NonNull Type type, @NonNull P payload) {
        super(type, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMSourceEvent(@NonNull Type type, @NonNull P payload, @CheckForNull String origin) {
        super(type, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    protected SCMSourceEvent(@NonNull SCMSourceEvent<P> src) {
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
     * Return a description of the event in the context of the supplied {@link SCMNavigator}.
     *
     * @param navigator the {@link SCMNavigator}, the navigator must be {@link #isMatch(SCMNavigator)}.
     * @return the description or {@code null} if no description can be provided.
     * @since 2.1.1
     */
    @CheckForNull
    public String descriptionFor(SCMNavigator navigator) {
        return description();
    }

    /**
     * Tests if this event applies to the supplied {@link SCMSource}. (Calling this method for a {@link Type#CREATED}
     * logically could return {@code true} if there has been out of order or delayed delivery of events)
     *
     * @param source the {@link SCMSource}, the source must be {@link #isMatch(SCMSource)}.
     * @return {@code true} if and only if this event concerns the supplied {@link SCMSource}.
     */
    public abstract boolean isMatch(@NonNull SCMSource source);

    /**
     * Return a description of the event in the context of the supplied {@link SCMSource}.
     *
     * @param source the {@link SCMSource}.
     * @return the description or {@code null} if no description can be provided.
     * @since 2.1.1
     */
    @CheckForNull
    public String descriptionFor(SCMSource source) {
        return description();
    }

    /**
     * Returns the name of the {@link SCMSource}, such as a repository name within an organization; may be used as an
     * {@link Item#getName}. Must be the same as the name that would be passed to
     * {@link SCMSourceObserver#observe(String)} by any {@link SCMNavigator} that {@link #isMatch(SCMNavigator)}.
     * <p>
     * <strong>DO NOT TRUST THE RETURN VALUES.</strong> Data from events should only be used as a rumour that requires
     * verification.
     *
     * @return the name of the {@link SCMSource}
     */
    @NonNull
    public abstract String getSourceName();

    /**
     * Fires the {@link SCMSourceEvent} to all registered {@link SCMEventListener} instances.
     *
     * @param event the event to fire.
     */
    public static void fireNow(@NonNull final SCMSourceEvent<?> event) {
        executorService().execute(new DispatcherImpl(event));
    }

    /**
     * Fires the {@link SCMSourceEvent} to all registered {@link SCMEventListener} instances after the specified delay.
     *
     * @param event      the event to fire.
     * @param delay      how long to wait before firing the event.
     * @param delayUnits the units of time in which the delay is expressed.
     */
    public static void fireLater(@NonNull final SCMSourceEvent<?> event, long delay, TimeUnit delayUnits) {
        executorService().schedule(new DispatcherImpl(event), delay, delayUnits);
    }

    private static class DispatcherImpl extends SCMEvent.Dispatcher<SCMSourceEvent<?>> {
        private DispatcherImpl(SCMSourceEvent<?> event) {
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
        protected void fire(SCMEventListener l, SCMSourceEvent<?> event) {
            l.onSCMSourceEvent(event);
        }
    }
}
