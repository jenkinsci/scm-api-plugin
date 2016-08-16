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
import hudson.plugins.git.GitSCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.scm.api.SCMSource;
import org.junit.Test;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

public class SingleSCMSourceTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void configRoundtrip() throws Exception {
        SingleSCMSource source = new SingleSCMSource("the-id", "the-name", new GitSCM("https://nowhere.net/something.git"));
        SCMSourceBuilder builder = new SCMSourceBuilder(source);
        r.assertEqualDataBoundBeans(builder, r.configRoundtrip(builder));
    }

    public static class SCMSourceBuilder extends Builder {

        public final SCMSource scm;

        @DataBoundConstructor
        public SCMSourceBuilder(SCMSource scm) {
            this.scm = scm;
        }

        @TestExtension("configRoundtrip")
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
