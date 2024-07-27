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
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.User;
import jenkins.scm.api.TrustworthyBuild;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class TrustworthyBuilds {

    // Also effectively handles ReplayCause since that is only ever added in conjunction with UserIdCause. (see ReplayAction.run2)
    @Extension
    public static TrustworthyBuild byUserId() {
        return (build, listener) -> {
            var cause = build.getCause(Cause.UserIdCause.class);
            if (cause == null) {
                // probably some other cause; do not print anything
                return false;
            }
            var userId = cause.getUserId();
            if (userId == null) {
                listener.getLogger().println("Not trusting build since no user name was recorded");
                return false;
            }
            var user = User.getById(userId, false);
            if (user == null) {
                listener.getLogger().printf("Not trusting build since no user ‘%s’ is known%n", userId);
                return false;
            }
            try {
                var permission = Run.PERMISSIONS.find("Replay"); // ReplayAction.REPLAY
                if (permission == null) { // no workflow-cps
                    permission = Item.CONFIGURE;
                }
                if (build.hasPermission2(user.impersonate2(), permission)) {
                    listener.getLogger().printf("Trusting build since it was started by user ‘%s’%n", userId);
                    return true;
                } else {
                    listener.getLogger().printf("Not trusting build since user ‘%s’ lacks %s/%s permission%n", userId, permission.group.title, permission.name);
                    return false;
                }
            } catch (UsernameNotFoundException x) {
                listener.getLogger().printf("Not trusting build since user ‘%s’ is invalid%n", userId);
                return false;
            }
        };
    }

    // TODO until github-checks can declare a dep on a sufficiently new scm-api
    @Extension
    public static TrustworthyBuild byGitHubChecks() {
        return (build, listener) -> {
            for (Cause cause : build.getCauses()) {
                if (cause.getClass().getName().equals("io.jenkins.plugins.checks.github.CheckRunGHEventSubscriber$GitHubChecksRerunActionCause")) {
                    listener.getLogger().println("Trusting build since it was a rerun request through GitHub checks API");
                    return true;
                }
            }
            return false;
        };
    }

    private TrustworthyBuilds() {}

}
