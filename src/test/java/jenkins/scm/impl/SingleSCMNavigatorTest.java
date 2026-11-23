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

package jenkins.scm.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class SingleSCMNavigatorTest {

    @Test
    void getName() {
        Random entropy = new Random();
        String name = "foo-" + entropy.nextLong();
        SingleSCMNavigator instance = new SingleSCMNavigator(name, Collections.emptyList());
        assertThat(instance.getName(), is(name));
    }

    @Test
    void getSources_empty() {
        assertThat(
                new SingleSCMNavigator("foo", Collections.emptyList()).getSources(),
                emptyCollectionOf(SCMSource.class));
    }

    @Test
    void getSources_one() {
        SCMSource s1 = mock(SCMSource.class);
        assertThat(new SingleSCMNavigator("foo", Collections.singletonList(s1)).getSources(), contains(s1));
    }

    @Test
    void getSources_two() {
        SCMSource s1 = mock(SCMSource.class);
        SCMSource s2 = mock(SCMSource.class);
        assertThat(new SingleSCMNavigator("foo", Arrays.asList(s1, s2)).getSources(), contains(s1, s2));
    }

    @Test
    void visitSources_empty() throws Exception {
        SCMSourceObserver mock = mock(SCMSourceObserver.class);
        SCMSourceObserver.ProjectObserver observer = mock(SCMSourceObserver.ProjectObserver.class);
        InOrder seq = inOrder(mock, observer);
        when(mock.observe("foo")).thenReturn(observer);
        new SingleSCMNavigator("foo", Collections.emptyList()).visitSources(mock);
        seq.verify(mock, times(1)).observe("foo");
        seq.verify(observer, never()).addSource(any(SCMSource.class));
        seq.verify(observer, never()).addAttribute(anyString(), anyString());
        seq.verify(observer, times(1)).complete();
        seq.verifyNoMoreInteractions();
    }

    @Test
    void visitSources_one() throws Exception {
        SCMSource s1 = mock(SCMSource.class);
        SCMSourceObserver mock = mock(SCMSourceObserver.class);
        SCMSourceObserver.ProjectObserver observer = mock(SCMSourceObserver.ProjectObserver.class);
        InOrder seq = inOrder(mock, observer);
        when(mock.observe("foo")).thenReturn(observer);
        new SingleSCMNavigator("foo", Collections.singletonList(s1)).visitSources(mock);
        seq.verify(mock, times(1)).observe("foo");
        seq.verify(observer, times(1)).addSource(s1);
        seq.verify(observer, never()).addAttribute(anyString(), anyString());
        seq.verify(observer, times(1)).complete();
        seq.verifyNoMoreInteractions();
    }

    @Test
    void visitSources_two() throws Exception {
        SCMSource s1 = mock(SCMSource.class);
        SCMSource s2 = mock(SCMSource.class);
        SCMSourceObserver mock = mock(SCMSourceObserver.class);
        SCMSourceObserver.ProjectObserver observer = mock(SCMSourceObserver.ProjectObserver.class);
        InOrder seq = inOrder(mock, observer);
        when(mock.observe("foo")).thenReturn(observer);
        new SingleSCMNavigator("foo", Arrays.asList(s1, s2)).visitSources(mock);
        seq.verify(mock, times(1)).observe("foo");
        seq.verify(observer, times(1)).addSource(s1);
        seq.verify(observer, times(1)).addSource(s2);
        seq.verify(observer, never()).addAttribute(anyString(), anyString());
        seq.verify(observer, times(1)).complete();
        seq.verifyNoMoreInteractions();
    }
}
