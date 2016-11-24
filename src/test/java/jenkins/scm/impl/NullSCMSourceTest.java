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

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceOwner;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.mockito.InOrder;
import org.mockito.Matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NullSCMSourceTest {

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void given_instance_when_fetch_then_noRevisionObserved() throws Exception {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        NullSCMSource instance = new NullSCMSource();
        instance.fetch(null, observer, null);
        verify(observer, never()).observe(
                (SCMHead)argThat(
                        allOf(
                                instanceOf(SCMHead.class),
                                hasProperty("name", is("the-name"))
                        )
                ),
                (SCMRevision) argThat(
                        allOf(
                                instanceOf(SCMRevision.class),
                                hasProperty("head", hasProperty("name", is("the-name"))),
                                hasProperty("deterministic", is(false))
                        )
                )
        );
    }

    @Test
    public void given_instance_when_fetchingNonObservedHead_then_nullScmReturned() throws Exception {
        NullSCMSource instance = new NullSCMSource();
        assertThat(instance.build(new SCMHead("foo"), mock(SCMRevision.class)), instanceOf(NullSCM.class));

    }

}
