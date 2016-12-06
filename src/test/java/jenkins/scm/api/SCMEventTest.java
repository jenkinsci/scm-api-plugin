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
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class SCMEventTest {

    @DataPoints
    public static SCMEvent.Type[] types() {
        return SCMEvent.Type.values();
    }

    @Test
    public void executorService() throws Exception {
        assertThat(SCMEvent.executorService(), notNullValue());
        assertThat(SCMEvent.executorService().isShutdown(), is(false));
    }

    @Theory
    public void getType(SCMEvent.Type type) throws Exception {
        assertThat(new MySCMEvent(type, new Object()).getType(), is(type));
    }

    @Test
    public void getTimestamp() throws Exception {
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
    public void getTimestamp2() throws Exception {
        MySCMEvent instance;
        instance = new MySCMEvent(SCMEvent.Type.CREATED, 53L, new Object());
        assertThat(instance.getTimestamp(), is(53L));
    }

    @Test
    public void getDate() throws Exception {
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
    public void getPayload() throws Exception {
        assertThat(new MySCMEvent(SCMEvent.Type.CREATED, this).getPayload(), sameInstance((Object) this));
    }

    @Test
    public void equalityContract() throws Exception {
        MySCMEvent a1 = new MySCMEvent(SCMEvent.Type.CREATED, 53L, "foo");
        MySCMEvent a2 = new MySCMEvent(SCMEvent.Type.CREATED, 53L, new String("foo"));
        MySCMEvent a3 = new MySCMEvent(SCMEvent.Type.CREATED, new Long(53L), new String("foo"));
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
    public void usefulToString() throws Exception {
        assertThat(new MySCMEvent(SCMEvent.Type.REMOVED, 1479764915000L, "{\"name\":\"value\"}").toString(),
                allOf(containsString(String.format("%tc", 1479764915000L)),
                        containsString("REMOVED"),
                        containsString("{\"name\":\"value\"}")
                )
        );
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
