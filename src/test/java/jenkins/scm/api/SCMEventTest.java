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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kohsuke.stapler.StaplerRequest2;

class SCMEventTest {

    @Test
    void executorService() {
        assertThat(SCMEvent.executorService(), notNullValue());
        assertThat(SCMEvent.executorService().isShutdown(), is(false));
    }

    @Test
    void eventProcessingMetricsReturnsZeroWhenNoEvents() {
        SCMEvent.EventQueueMetrics eventProcessingMetrics = SCMEvent.getEventProcessingMetrics();
        assertThat(eventProcessingMetrics, notNullValue());
        assertThat(eventProcessingMetrics.getPoolSize(), is(0));
        assertThat(eventProcessingMetrics.getActiveThreads(), is(0));
        assertThat(eventProcessingMetrics.getQueuedTasks(), is(0));
        assertThat(eventProcessingMetrics.getCompletedTasks(), is(0L));
    }

    @Test
    void eventProcessingMetrics() {

        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        when(executor.getPoolSize()).thenReturn(20);
        when(executor.getActiveCount()).thenReturn(30);

        @SuppressWarnings("unchecked")
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) mock(BlockingQueue.class);

        when(queue.size()).thenReturn(500);

        when(executor.getQueue()).thenReturn(queue);
        when(executor.getCompletedTaskCount()).thenReturn(1000L);

        SCMEvent.EventQueueMetrics eventProcessingMetrics = new SCMEvent.EventQueueMetrics(executor);
        assertThat(eventProcessingMetrics, notNullValue());
        assertThat(eventProcessingMetrics.getPoolSize(), is(20));
        assertThat(eventProcessingMetrics.getActiveThreads(), is(30));
        assertThat(eventProcessingMetrics.getQueuedTasks(), is(500));
        assertThat(eventProcessingMetrics.getCompletedTasks(), is(1000L));
    }

    @ParameterizedTest
    @EnumSource(SCMEvent.Type.class)
    void getType(SCMEvent.Type type) {
        assertThat(new MySCMEvent(type, new Object()).getType(), is(type));
    }

    @Test
    void getTimestamp() {
        long before = System.currentTimeMillis();
        long after;
        MySCMEvent instance;
        try {
            instance = new MySCMEvent(SCMEvent.Type.CREATED, new Object());
        } finally {
            after = System.currentTimeMillis();
        }
        assertThat(instance.getTimestamp(), allOf(lessThanOrEqualTo(after), greaterThanOrEqualTo(before)));
    }

    @Test
    void getTimestamp2() {
        MySCMEvent instance;
        instance = new MySCMEvent(SCMEvent.Type.CREATED, 53L, new Object());
        assertThat(instance.getTimestamp(), is(53L));
    }

    @Test
    void getDate() {
        long before = System.currentTimeMillis();
        long after;
        MySCMEvent instance;
        try {
            instance = new MySCMEvent(SCMEvent.Type.CREATED, new Object());
        } finally {
            after = System.currentTimeMillis();
        }
        assertThat(instance.getDate().getTime(), allOf(lessThanOrEqualTo(after), greaterThanOrEqualTo(before)));
    }

    @Test
    void getPayload() {
        assertThat(new MySCMEvent(SCMEvent.Type.CREATED, this).getPayload(), sameInstance(this));
    }

    @Test
    void equalityContract() {
        MySCMEvent a1 = new MySCMEvent(SCMEvent.Type.CREATED, 53L, "foo");
        MySCMEvent a2 = new MySCMEvent(SCMEvent.Type.CREATED, 53L, new String("foo"));
        MySCMEvent a3 = new MySCMEvent(SCMEvent.Type.CREATED, Long.valueOf(53L), new String("foo"));
        MySCMEvent b1 = new MySCMEvent(SCMEvent.Type.UPDATED, 53L, "foo");
        MySCMEvent c1 = new MySCMEvent(SCMEvent.Type.CREATED, 54L, "foo");
        MySCMEvent d1 = new MySCMEvent(SCMEvent.Type.CREATED, 53L, "bar");
        assertThat(a1, is(a1));
        assertThat(a1.hashCode(), is(a1.hashCode()));
        assertThat(a1, is(a2));
        assertThat(a1.hashCode(), is(a2.hashCode()));
        assertThat(a2, is(a1));
        assertThat(a2, is(a3));
        assertThat(a1, is(a3));
        assertThat(a1, not(is(b1)));
        assertThat(a1, not(is(c1)));
        assertThat(a1, not(is(d1)));
    }

    @Test
    void usefulToString() {
        assertThat(
                new MySCMEvent(SCMEvent.Type.REMOVED, 1479764915000L, "{\"name\":\"value\"}").toString(),
                allOf(
                        containsString(String.format("%tc", 1479764915000L)),
                        containsString("REMOVED"),
                        containsString("{\"name\":\"value\"}")));
    }

    @Test
    void originOfNull() {
        assertThat(SCMEvent.originOf((StaplerRequest2) null), is(nullValue()));
    }

    @Test
    void originOfSimpleRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getLocalPort()).thenReturn(80);
        when(req.getRemoteHost()).thenReturn("scm.example.com");
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req), is("scm.example.com/203.0.113.1 ⇒ http://jenkins.example.com/jenkins/notify"));
    }

    @Test
    void originOfSimpleTLSRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("https");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getLocalPort()).thenReturn(443);
        when(req.getRemoteHost()).thenReturn("scm.example.com");
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req), is("scm.example.com/203.0.113.1 ⇒ https://jenkins.example.com/jenkins/notify"));
    }

    @Test
    void originOfSimpleRequestNonStdPort() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getLocalPort()).thenReturn(8080);
        when(req.getRemoteHost()).thenReturn("scm.example.com");
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req),
                is("scm.example.com/203.0.113.1 ⇒ http://jenkins.example.com:8080/jenkins/notify"));
    }

    @Test
    void originOfSimpleTLSRequestNonStdPort() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("https");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getLocalPort()).thenReturn(8443);
        when(req.getRemoteHost()).thenReturn("scm.example.com");
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req),
                is("scm.example.com/203.0.113.1 ⇒ https://jenkins.example.com:8443/jenkins/notify"));
    }

    @Test
    void originOfForwardedRequestSingleHop() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(req.getHeader("X-Forwarded-Port")).thenReturn("443");
        when(req.getHeader("X-Forwarded-For")).thenReturn("scm.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getLocalPort()).thenReturn(8080);
        when(req.getRemoteHost()).thenReturn("proxy.example.com");
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req),
                is("scm.example.com → proxy.example.com/203.0.113.1 ⇒ https://jenkins.example.com/jenkins/notify"));
    }

    @Test
    void originOfForwardedRequestMultiHop() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("jenkins.example.com");
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(req.getHeader("X-Forwarded-Port")).thenReturn("443");
        when(req.getHeader("X-Forwarded-For")).thenReturn("scm.example.com, gateway.example.com, proxy.example.com");
        when(req.getRequestURI()).thenReturn("/jenkins/notify");
        when(req.getRemotePort()).thenReturn(8080);
        when(req.getRemoteHost()).thenReturn(null);
        when(req.getRemoteAddr()).thenReturn("203.0.113.1");
        assertThat(
                SCMEvent.originOf(req),
                is(
                        "scm.example.com → gateway.example.com → proxy.example.com → 203.0.113.1 ⇒ https://jenkins.example.com/jenkins/notify"));
    }

    public static class MySCMEvent extends SCMEvent<Object> {

        private final Long widget;

        public MySCMEvent(@NonNull Type type, long timestamp, @NonNull Object payload) {
            super(type, timestamp, payload);
            widget = 1L;
        }

        public MySCMEvent(@NonNull Type type, @NonNull Object payload) {
            super(type, payload);
            widget = 1L;
        }

        protected MySCMEvent(MySCMEvent copy) {
            super(copy);
            widget = copy.widget == null ? 0L : copy.widget;
        }

        private Object readResolve() {
            if (widget == null) {
                return new MySCMEvent(this);
            }
            return this;
        }
    }
}
