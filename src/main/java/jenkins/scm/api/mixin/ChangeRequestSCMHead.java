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

package jenkins.scm.api.mixin;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.export.Exported;

/**
 * Mixin interface to identify {@link SCMHead} instances that correspond to a change request.
 *
 * @since 2.0
 */
public interface ChangeRequestSCMHead extends SCMHeadMixin {

    /**
     * Identifier of this change request.
     * Expected to be unique among requests coming from a given {@link SCMSource}.
     *
     * @return an ID of some kind, such as a pull request number (decimal) or a Gerrit change ID
     */
    @Exported
    @NonNull
    String getId();

    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} of this {@link ChangeRequestSCMHead}.
     *
     * @return the {@link ChangeRequestCheckoutStrategy}.
     * @since TODO
     */
    @NonNull
    default ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return null;
    }

    /**
     * Returns the name of the actual head on the source control system which may or may not be different from
     * {@link #getName()}. For example in GitHub or Bitbucket this method would return the name of the origin branch
     * whereas {@link #getName()} would return something like {@code PR-24}. It is perfectly acceptable for a SCM
     * implementation to return the same value as {@link #getName()} where the SCM implementation does not have a
     * separate concept of origin name.
     *
     * @return the name this {@link ChangeRequestSCMHead} would have if the {@link SCMSource} were configured
     * against the {@link #getOrigin()} directly and the change request were be discoverable as a regular
     * {@link SCMHead} or {@link #getName()} if such a concept is not possible in the backing source control system.
     *
     * @since TODO
     */
    @NonNull
    @Exported
    default String getOriginName() {
        return null;
    }

    /**
     * Branch to which this change would be merged or applied if it were accepted.
     *
     * @return a "target" or "base" branch
     */
    @Exported
    @NonNull
    SCMHead getTarget();

    /**
     * Returns the title of the change request
     * @return title of the change request
     */
    @Exported
    @NonNull
    default String getTitle() {
        return null;
    }
}
