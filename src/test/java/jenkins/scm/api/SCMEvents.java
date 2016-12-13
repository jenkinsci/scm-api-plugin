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

import java.util.concurrent.TimeUnit;

/**
 * Utility class to help testing SCM events.
 *
 * @since 2.0.1
 */
public class SCMEvents {
    /**
     * Returns the current watermark.
     *
     * @return the current watermark.
     */
    public static long getWatermark() {
        return SCMEvent.Dispatcher.lastId.get();
    }

    /**
     * Waits until at least one event has been processed past the supplied watermark.
     *
     * @param watermark the watermark.
     * @throws InterruptedException if interrupted.
     */
    public static void awaitOne(long watermark)
            throws InterruptedException {
        SCMEvent.Dispatcher.lock.lock();
        try {
            while (true) {
                if (SCMEvent.Dispatcher.finishedId > watermark) {
                    return;
                }
                SCMEvent.Dispatcher.finished.await();
            }
        } finally {
            SCMEvent.Dispatcher.lock.unlock();
        }
    }

    /**
     * Waits until at least one event has been processed past the supplied watermark or the timeout expires.
     *
     * @param watermark the watermark.
     * @param maxWait the time to wait.
     * @param unit the units of time.
     * @return {@code false} if the waiting time detectably elapsed
     * before return from the method, else {@code true}
     * @throws InterruptedException  if interrupted.
     */
    public static boolean awaitOne(long watermark, long maxWait, TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(maxWait);
        SCMEvent.Dispatcher.lock.lock();
        try {
            while (nanos > 0L) {
                if (SCMEvent.Dispatcher.finishedId > watermark) {
                    return true;
                }
                nanos = SCMEvent.Dispatcher.finished.awaitNanos(nanos);
            }
            return false;
        } finally {
            SCMEvent.Dispatcher.lock.unlock();
        }
    }

    /**
     * Waits until both at least one event has been processed past the supplied watermark and all in-flight events have
     * been processed.
     *
     * @param watermark the watermark.
     * @throws InterruptedException if interrupted.
     */
    public static void awaitAll(long watermark) throws InterruptedException {
        SCMEvent.Dispatcher.lock.lock();
        try {
            while (true) {
                if (SCMEvent.Dispatcher.finishedId == Math.max(watermark, Math.max(
                        SCMEvent.Dispatcher.startedId, SCMEvent.Dispatcher.lastId.get()))) {
                    return;
                }
                SCMEvent.Dispatcher.finished.await();
            }
        } finally {
            SCMEvent.Dispatcher.lock.unlock();
        }
    }

    /**
     * Waits until both at least one event has been processed past the supplied watermark and all in-flight events have
     * been processed or the timeout expires.
     *
     * @param watermark the watermark.
     * @param maxWait   the time to wait.
     * @param unit      the units of time.
     * @return {@code false} if the waiting time detectably elapsed
     * before return from the method, else {@code true}
     * @throws InterruptedException if interrupted.
     */
    public static boolean awaitAll(long watermark, long maxWait, TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(maxWait);
        SCMEvent.Dispatcher.lock.lock();
        try {
            while (nanos > 0L) {
                if (SCMEvent.Dispatcher.finishedId == Math.max(watermark, Math.max(
                        SCMEvent.Dispatcher.startedId, SCMEvent.Dispatcher.lastId.get()))) {
                    return true;
                }
                nanos = SCMEvent.Dispatcher.finished.awaitNanos(nanos);
            }
            return false;
        } finally {
            SCMEvent.Dispatcher.lock.unlock();
        }
    }

}
