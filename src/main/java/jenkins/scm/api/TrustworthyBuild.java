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

package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;

/**
 * Allows plugins to declare that builds were triggered deliberately.
 * This allows an authorized user to run CI on (say) a pull request filed by an outsider,
 * having confirmed that there is nothing malicious about the contents.
 * @see SCMSource#getTrustedRevisionForBuild
 */
public interface TrustworthyBuild extends ExtensionPoint {

    /**
     * Should this build be trusted to load sensitive source files?
     * If any implementation returns true then it is trusted.
     */
    boolean shouldBeTrusted(@NonNull Run<?, ?> build);

    /**
     * Convenience for the common case that a particular trigger cause indicates trust.
     * Examples of causes which could be trusted include:
     * <ul>
     * <li>{@link Cause.UserIdCause}
     * <li>{@code ReplayCause}
     * <li>{@code CheckRunGHEventSubscriber.GitHubChecksRerunActionCause}
     * </ul>
     * Examples of causes which should <em>not</em> be registered include:
     * <ul>
     * <li>{@link TimerTrigger.TimerTriggerCause}
     * <li>{@link SCMTrigger.SCMTriggerCause}
     * <li>{@code BranchIndexingCause}
     * <li>{@code BranchEventCause}
     * </ul>
     */
    static TrustworthyBuild byCause(Class<? extends Cause> causeType) {
        return build -> build.getCause(causeType) != null;
    }

}
