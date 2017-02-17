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

package jenkins.scm.impl.mock;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Represents latency in connecting to the {@link MockSCM}.
 *
 * @since 2.0.5
 */
public abstract class MockLatency {

    private static final MockLatency NONE = new MockLatency() {
        @Override
        public void apply() throws InterruptedException {
        }
    };
    private static final MockLatency YIELD = new MockLatency() {
        @Override
        public void apply() throws InterruptedException {
            Thread.yield();
        }
    };

    public abstract void apply() throws InterruptedException;

    /**
     * A fixed latency.
     * @param time the latency.
     * @param units the units.
     * @return a fixed latency.
     */
    public static MockLatency fixed(final long time, final TimeUnit units) {
        return new MockLatency() {
            @Override
            public void apply() throws InterruptedException {
                units.sleep(time);
            }
        };
    }

    /**
     * A fixed latency for all threads except the current thread.
     * 
     * @param time the latency.
     * @param units the units.
     * @return a fixed latency.
     */
    public static MockLatency fixedForOtherThreads(final long time, final TimeUnit units) {
        final Thread safe = Thread.currentThread();
        return new MockLatency() {
            @Override
            public void apply() throws InterruptedException {
                if (Thread.currentThread() != safe) {
                    units.sleep(time);
                }
            }
        };
    }

    /**
     * A random latency that has an expected average time.
     *
     * @param time  the expected average latency.
     * @param units the units.
     * @return a fixed latency.
     */
    public static MockLatency average(final long time, final TimeUnit units) {
        return new MockLatency() {
            final Random entropy = new Random();
            @Override
            public void apply() throws InterruptedException {
                long ms = units.toMillis(time);
                ms = Math.min(ms * 3L, Math.max((long) (ms + (ms * entropy.nextGaussian())), 1L));
                Thread.sleep(ms);
            }
        };
    }
    /**
     * A latency that just forces the thread scheduler to yield.
     *
     * @return a minimal latency that causes background threads to run.
     */
    public static MockLatency yield() {
        return YIELD;
    }
    /**
     * A latency that just forces the thread scheduler to yield.
     *
     * @return a minimal latency that causes background threads to run.
     */
    public static MockLatency none() {
        return NONE;
    }
}
