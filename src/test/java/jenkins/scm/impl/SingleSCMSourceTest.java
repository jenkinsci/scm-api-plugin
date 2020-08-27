/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
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
 */

package jenkins.scm.impl;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
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
import jenkins.scm.impl.mock.MockSCM;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.mockito.InOrder;
import org.mockito.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class SingleSCMSourceTest {

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void configRoundtrip() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SingleSCMSource source = new SingleSCMSource(
                    "the-name",
                    new MockSCM(
                            c,
                            "foo",
                            new MockSCMHead("master"),
                            null
                    )
            );
            source.setId("the-id");
            SCMSourceBuilder builder = new SCMSourceBuilder(source);
            SingleSCMSource expected = new SingleSCMSource(
                    "the-name",
                    new MockSCM(
                            c,
                            "foo",
                            new MockSCMHead("master"),
                            null
                    )
            );
            expected.setId("the-id");
            SCMSource actual = r.configRoundtrip(builder).scm;
            System.out.printf("expected=%s%nactual=%s%n", expected, actual);
            r.assertEqualDataBoundBeans(expected, actual);
        }
    }

    @Test
    public void given_instance_when_fetch_then_revisionObserved() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SCMHeadObserver observer = mock(SCMHeadObserver.class);
            SingleSCMSource instance = new SingleSCMSource("the-id", "the-name",
                    new MockSCM(c, "foo", new MockSCMHead("master"), null));
            instance.fetch(null, observer, null);
            verify(observer).observe(
                    argThat(
                            allOf(
                                    instanceOf(SCMHead.class),
                                    hasProperty("name", is("the-name"))
                            )
                    ),
                    argThat(
                            allOf(
                                    instanceOf(SCMRevision.class),
                                    hasProperty("head", hasProperty("name", is("the-name"))),
                                    hasProperty("deterministic", is(false))
                            )
                    )
            );
        }
    }

    @Test
    public void given_instance_when_fetchWithCriteria_then_criteriaIgnoredAndRevisionObserved() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SCMHeadObserver observer = mock(SCMHeadObserver.class);
            SCMSourceCriteria criteria = mock(SCMSourceCriteria.class);
            InOrder seq = inOrder(observer, criteria);
            SingleSCMSource instance = new SingleSCMSource("the-id", "the-name",
                    new MockSCM(c, "foo", new MockSCMHead("master"), null));
            instance.fetch(criteria, observer, null);
            seq.verify(observer).observe(
                    (SCMHead) argThat(
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
            seq.verifyNoMoreInteractions();
        }
    }

    @Test
    public void given_instance_when_fetchingObservedHead_then_scmReturned() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SingleSCMSource instance = new SingleSCMSource("the-id", "the-name",
                    new MockSCM(c, "foo", new MockSCMHead("master"), null));
            Map<SCMHead, SCMRevision> result = instance.fetch(SCMHeadObserver.collect(), null).result();
            assertThat(result.entrySet(), hasSize(1));
            Map.Entry<SCMHead, SCMRevision> entry = result.entrySet().iterator().next();
            assertThat(instance.build(entry.getKey(), entry.getValue()), instanceOf(MockSCM.class));
        }
    }

    @Test
    public void given_instance_when_fetchingNonObservedHead_then_nullScmReturned() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SingleSCMSource instance = new SingleSCMSource("the-id", "the-name",
                    new MockSCM(c, "foo", new MockSCMHead("master"), null));
            assertThat(instance.build(new SCMHead("foo"), mock(SCMRevision.class)), instanceOf(NullSCM.class));
        }
    }

    @Test
    public void scmRevisionImpl() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            SingleSCMSource instance = new SingleSCMSource("the-id", "the-name",
                    new MockSCM(c, "foo", new MockSCMHead("master"), null));
            Map<SCMHead, SCMRevision> result = instance.fetch(SCMHeadObserver.collect(), null).result();
            SCMRevision revision = result.values().iterator().next();
            assertThat(revision.isDeterministic(), is(false));
            assertThat(revision.equals(revision), is(true));
            assertThat(revision.equals(mock(SCMRevision.class)), is(false));
            assertThat(revision.hashCode(), is(result.keySet().iterator().next().hashCode()));
        }
    }

    @Test
    public void getSCMDescriptors() throws Exception {
        TopLevelItemDescriptor descriptor = mock(TopLevelItemDescriptor.class);
        TopLevelSCMOwner owner = mock(TopLevelSCMOwner.class);
        when(owner.getDescriptor()).thenReturn(descriptor);
        when(descriptor.isApplicable(Matchers.any(Descriptor.class))).thenReturn(true);
        assertThat(SingleSCMSource.DescriptorImpl.getSCMDescriptors(owner),
                (Matcher) hasItem(instanceOf(MockSCM.DescriptorImpl.class)));
    }

    public interface TopLevelSCMOwner extends TopLevelItem, SCMSourceOwner {
    }

    /**
     * Helper for {@link #configRoundtrip()}.
     */
    public static class SCMSourceBuilder extends Builder {

        public final SCMSource scm;

        @DataBoundConstructor
        public SCMSourceBuilder(SCMSource scm) {
            this.scm = scm;
        }

        @TestExtension
        public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

            @Override
            public String getDisplayName() {
                return "SCMSourceBuilder";
            }

            @SuppressWarnings("rawtypes")
            @Override
            public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                return true;
            }

        }

    }

}
