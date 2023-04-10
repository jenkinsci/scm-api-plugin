/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

package jenkins.scm.impl.mock;

import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.triggers.SCMTrigger;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Basis for {@link Rule} classes which run a concrete SCM tool on a sample repository, for integration testing.
 *
 * @since 2.0.8
 */
public abstract class AbstractSampleRepoRule extends ExternalResource {

    /**
     * Run a given command.
     * @param probing if true, throw {@link AssumptionViolatedException} rather than {@link AssertionError} in case of problems
     * @param cwd working directory to use
     * @param cmds command and arguments to run
     */
    public static void run(boolean probing, File cwd, String... cmds) throws Exception {
        try {
            TaskListener listener = StreamTaskListener.fromStdout();
            int r = new Launcher.LocalLauncher(listener).launch().cmds(cmds).pwd(cwd).stdout(listener).join();
            String message = Arrays.toString(cmds) + " failed with error code";
            if (probing) {
                Assume.assumeThat(message, r, is(0));
            } else {
                assertThat(message, r, is(0));
            }
        } catch (Exception x) {
            if (probing) {
                Assume.assumeNoException(Arrays.toString(cmds) + " failed with exception (required tooling not installed?)", x);
            } else {
                throw x;
            }
        }
    }

    protected final TemporaryFolder tmp;

    protected AbstractSampleRepoRule() {
        this.tmp = new TemporaryFolder();
    }

    @Override
    protected void before() throws Throwable {
        tmp.create();
    }

    @Override
    protected void after() {
        tmp.delete();
    }

    /** Otherwise {@link JenkinsRule#waitUntilNoActivity()} is ineffective when we have just pinged a commit notification endpoint. */
    protected final void synchronousPolling(JenkinsRule r) {
        r.jenkins.getDescriptorByType(SCMTrigger.DescriptorImpl.class).synchronousPolling = true;
    }

}
