/*
 * The MIT License
 *
 * Copyright 2022 CloudBees, Inc.
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

import hudson.Extension;
import hudson.model.Cause;
import jenkins.scm.api.TrustworthyBuild;

public class TrustworthyBuilds {

    // Also effectively handles ReplayCause since that is only ever added in conjunction with UserIdCause. (see ReplayAction.run2)
    @Extension
    public static TrustworthyBuild byUserId() {
        return TrustworthyBuild.byCause(Cause.UserIdCause.class);
    }

    // TODO until github-checks can declare a dep on a sufficiently new scm-api
    @Extension
    public static TrustworthyBuild byGitHubChecks() {
        return build -> build.getCauses().stream().anyMatch(cause -> cause.getClass().getName().equals("io.jenkins.plugins.checks.github.CheckRunGHEventSubscriber$GitHubChecksRerunActionCause"));
    }

    private TrustworthyBuilds() {}

}
