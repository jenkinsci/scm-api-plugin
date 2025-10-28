/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import java.io.IOException;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.impl.mock.MockSCM;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SCMFileSystemTest {

    @Issue("JENKINS-52964")
    @Test
    void filesystem_supports_false_by_default_for_descriptor(JenkinsRule r) {
        SCMSourceDescriptor descriptor = r.jenkins.getDescriptorByType(MockSCMSource.DescriptorImpl.class);

        assertFalse(SCMFileSystem.supports(descriptor));

        SCMDescriptor<MockSCM> scmDescriptor = r.jenkins.getDescriptorByType(MockSCM.DescriptorImpl.class);

        assertFalse(SCMFileSystem.supports(scmDescriptor));
    }

    @Issue("JENKINS-52964")
    @Test
    void filesystem_supports_true_implementation_for_descriptor(JenkinsRule r) {
        SCMSourceDescriptor descriptor = r.jenkins.getDescriptorByType(MockSCMSource.DescriptorImpl.class);

        assertTrue(SCMFileSystem.supports(descriptor));

        SCMDescriptor<MockSCM> scmDescriptor = r.jenkins.getDescriptorByType(MockSCM.DescriptorImpl.class);

        assertTrue(SCMFileSystem.supports(scmDescriptor));
    }

    @Issue("JENKINS-52964")
    @Test
    void filesystem_supports_false_implementation_for_descriptor(JenkinsRule r) {
        SCMSourceDescriptor descriptor = r.jenkins.getDescriptorByType(MockSCMSource.DescriptorImpl.class);

        assertFalse(SCMFileSystem.supports(descriptor));

        SCMDescriptor<MockSCM> scmDescriptor = r.jenkins.getDescriptorByType(MockSCM.DescriptorImpl.class);

        assertFalse(SCMFileSystem.supports(scmDescriptor));
    }

    @TestExtension("filesystem_supports_true_implementation_for_descriptor")
    public static class TrueBuilder extends SCMFileSystem.Builder {
        @Override
        public boolean supports(SCM source) {
            return source instanceof MockSCM;
        }

        @Override
        public boolean supports(SCMSource source) {
            return source instanceof MockSCMSource;
        }

        @Override
        protected boolean supportsDescriptor(SCMDescriptor descriptor) {
            System.err.println("SUP: " + descriptor);
            return descriptor instanceof MockSCM.DescriptorImpl;
        }

        @Override
        protected boolean supportsDescriptor(SCMSourceDescriptor descriptor) {
            System.err.println("SRC SUP: " + descriptor);
            return descriptor instanceof MockSCMSource.DescriptorImpl;
        }

        @Override
        @CheckForNull
        public SCMFileSystem build(@NonNull Item owner, @NonNull SCM scm, @CheckForNull SCMRevision rev)
                throws IOException, InterruptedException {
            return null;
        }
    }

    @TestExtension("filesystem_supports_false_implementation_for_descriptor")
    public static class FalseBuilder extends SCMFileSystem.Builder {
        @Override
        public boolean supports(SCM source) {
            return !(source instanceof MockSCM);
        }

        @Override
        public boolean supports(SCMSource source) {
            return !(source instanceof MockSCMSource);
        }

        @Override
        protected boolean supportsDescriptor(SCMDescriptor descriptor) {
            return !(descriptor instanceof MockSCM.DescriptorImpl);
        }

        @Override
        protected boolean supportsDescriptor(SCMSourceDescriptor descriptor) {
            return !(descriptor instanceof MockSCMSource.DescriptorImpl);
        }

        @Override
        @CheckForNull
        public SCMFileSystem build(@NonNull Item owner, @NonNull SCM scm, @CheckForNull SCMRevision rev)
                throws IOException, InterruptedException {
            return null;
        }
    }
}
