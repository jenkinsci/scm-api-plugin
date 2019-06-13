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

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SCMHeadObserverTest {
    @Test
    public void allOf() throws Exception {
        SCMHead head1 = new SCMHead("bar");
        SCMRevision revision1 = mock(SCMRevision.class);
        SCMHead head2 = new SCMHead("foo");
        SCMRevision revision2 = mock(SCMRevision.class);
        SCMHeadObserver.AllFinished instance =
                SCMHeadObserver.allOf(SCMHeadObserver.select(head2), SCMHeadObserver.select(head1));
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants everything", instance.getIncludes(), containsInAnyOrder(head1, head2));
        instance.observe(head1, revision1);
        assertThat("Stops when all of the observers have stopped", instance.isObserving(), is(true));
        instance.observe(head2, revision2);
        assertThat("Stops when all of the observers have stopped", instance.isObserving(), is(false));
    }

    @Test
    public void first() throws Exception {
        SCMHeadObserver.OneFinished instance = SCMHeadObserver.first(SCMHeadObserver.collect(), SCMHeadObserver.any());
        SCMHead head1 = new SCMHead("bar");
        SCMRevision revision1 = mock(SCMRevision.class);
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants everything", instance.getIncludes(), nullValue());
        instance.observe(head1, revision1);
        assertThat("Stops when one of the observers has stopped", instance.isObserving(), is(false));
    }

    @Test
    public void collect() throws Exception {
        SCMHead head1 = new SCMHead("bar");
        SCMRevision revision1 = mock(SCMRevision.class);
        SCMHead head2 = new SCMHead("foo");
        SCMRevision revision2 = mock(SCMRevision.class);
        SCMHeadObserver.Collector instance = SCMHeadObserver.collect();
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants everything", instance.getIncludes(), nullValue());
        instance.observe(head1, revision1);
        assertThat("Still observing", instance.isObserving(), is(true));
        instance.observe(head2, revision2);
        assertThat("Still observing", instance.isObserving(), is(true));
        assertThat(instance.result(), Matchers.<Map<SCMHead,SCMRevision>>allOf(hasEntry(head1, revision1), hasEntry(head2, revision2)));

    }

    @Test
    public void select() throws Exception {
        SCMHead head1 = new SCMHead("bar");
        SCMRevision revision1 = mock(SCMRevision.class);
        SCMHead head2 = new SCMHead("foo");
        SCMRevision revision2 = mock(SCMRevision.class);
        SCMHeadObserver.Selector instance = SCMHeadObserver.select(head2);
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants only selected head", instance.getIncludes(), contains(head2));
        instance.observe(head1, revision1);
        assertThat("Still observing before match", instance.isObserving(), is(true));
        instance.observe(head2, revision2);
        assertThat("Stops observing after selected observation", instance.isObserving(), is(false));
        assertThat(instance.result(), is(revision2));
    }

    @Test
    public void filter() throws Exception {
        SCMHead head1 = new SCMHead("bar");
        SCMRevision revision1 = mock(SCMRevision.class);
        SCMHead head2 = new SCMHead("foo");
        SCMRevision revision2 = mock(SCMRevision.class);
        SCMHeadObserver.Filter<SCMHeadObserver.Collector>
                instance = SCMHeadObserver.filter(SCMHeadObserver.collect(), head2);
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants only selected head", instance.getIncludes(), contains(head2));
        instance.observe(head1, revision1);
        assertThat("Still observing before match", instance.isObserving(), is(true));
        instance.observe(head2, revision2);
        assertThat("Stops observing after selected observation", instance.isObserving(), is(false));
        assertThat(instance.unwrap().result(), hasEntry(head2,revision2));
        assertThat(instance.unwrap().result(), not(hasKey(head1)));

    }

    @Test
    public void named() throws Exception {
        SCMHeadObserver.Named instance = SCMHeadObserver.named("foo");
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants everything", instance.getIncludes(), nullValue());
        SCMHead head1 = mock(SCMHead.class);
        SCMRevision revision1 = mock(SCMRevision.class);
        SCMHead head2 = mock(SCMHead.class);
        SCMRevision revision2 = mock(SCMRevision.class);
        when(head1.getName()).thenReturn("bar");
        when(head2.getName()).thenReturn("foo");
        instance.observe(head1, revision1);
        assertThat("Still observing before match", instance.isObserving(), is(true));
        instance.observe(head2, revision2);
        assertThat("Stops observing after matching observation", instance.isObserving(), is(false));
        assertThat(instance.result(), is(revision2));
    }

    @Test
    public void any() throws Exception {
        SCMHeadObserver.Any instance = SCMHeadObserver.any();
        assertThat("Observing from the start", instance.isObserving(), is(true));
        assertThat("Wants everything", instance.getIncludes(), nullValue());
        SCMHead head = mock(SCMHead.class);
        SCMRevision revision = mock(SCMRevision.class);
        instance.observe(head, revision);
        assertThat("Stops observing after first observation", instance.isObserving(), is(false));
        assertThat(instance.result(), is(revision));
    }

}
