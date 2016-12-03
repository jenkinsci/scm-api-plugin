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
    public SCMNavigatorEvent(@NonNull Type type, long timestamp, @NonNull P payload) {
        super(type, timestamp, payload);
    }

    /**
     * {@inheritDoc}
     */
    public SCMNavigatorEvent(@NonNull Type type, @NonNull P payload) {
        super(type, payload);
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
     * Fires the {@link SCMNavigatorEvent} to all registered {@link SCMEventListener} instances.
     *
     * @param event the event to fire.
     */
    public static void fireNow(@NonNull final SCMNavigatorEvent<?> event) {
        executorService().execute(new SCMNavigatorEvent.Dispatcher(event));
    }

    /**
     * Fires the {@link SCMNavigatorEvent} to all registered {@link SCMEventListener} instances after the specified delay.
     *
     * @param event      the event to fire.
     * @param delay      how long to wait before firing the event.
     * @param delayUnits the units of time in which the delay is expressed.
     */
    public static void fireLater(@NonNull final SCMNavigatorEvent<?> event, long delay, TimeUnit delayUnits) {
        executorService().schedule(new SCMNavigatorEvent.Dispatcher(event), delay, delayUnits);
    }

    private static class Dispatcher implements Runnable {
        private final SCMNavigatorEvent<?> event;

        public Dispatcher(SCMNavigatorEvent<?> event) {
            this.event = event;
        }

        @Override
        public void run() {
            String oldName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(String.format("SCMNavigatorEvent %tc / %s",
                        event.getTimestamp(), oldName)
                );
                for (final SCMEventListener l : ExtensionList.lookup(SCMEventListener.class)) {
                    try {
                        l.onSCMNavigatorEvent(event);
                    } catch (LinkageError e) {
                        LogRecord lr = new LogRecord(Level.WARNING,
                                "SCMEventListener.onSCMNavigatorEvent(onSCMNavigatorEvent) {0} propagated an exception"
                        );
                        lr.setThrown(e);
                        lr.setParameters(new Object[]{l});
                        LOGGER.log(lr);
                    } catch (Error e) {
                        throw e;
                    } catch (Throwable e) {
                        LogRecord lr = new LogRecord(Level.WARNING,
                                "SCMEventListener.onSCMNavigatorEvent(SCMNavigatorEvent) {0} propagated an exception"
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

}
