/*
 * The MIT License
 *
 * Copyright (c) 2017 CloudBees, Inc.
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

package jenkins.scm.api.mixin;

import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

/**
 * A change request that can be considered as having an independent {@link SCMRevision} that can be checked out without
 * reference to a revision of the {@link #getTarget()}. To clarify the subtyping we will provide an example of a
 * {@link ChangeRequestSCMHead} that is <strong>not</strong> a {@link MergeableChangeRequestSCMHead}:
 * <ul>
 * <li>
 * A {@link SCMSource} such as
 * <a href="https://www.reviewboard.org/">Review Board</a> when used in "patch" mode would store change requests as
 * a DIFF against the backing source control system. As such, we cannot just checkout the DIFF on its own. We need to
 * checkout a revision of the {@link #getTarget()} and then apply the DIFF. Thus this cannot be a
 * {@link MergeableChangeRequestSCMHead}.
 * </li>
 * </ul>
 * Here is an example of a {@link MergeableChangeRequestSCMHead}:
 * <ul>
 * <li>
 * GitHub / Bitbucket / most DVCS systems typically implement change requests as branches. We have then the choice to
 * either can check out the revision of the branch or merge the revision of the branch onto a specified revision
 * of the {@link #getTarget()}. This type of change request is therefore a {@link MergeableChangeRequestSCMHead}.
 * </li>
 * </ul>
 *
 * @since 2.2.0
 */
public interface MergeableChangeRequestSCMHead extends ChangeRequestSCMHead {
    /**
     * Is this a {@link MergeableChangeRequestSCMHead} that performs a merge of the change request revision against the
     * target revision?
     *
     * @return {@code true} if the effective revision is to be a merge of the change request revision onto the target
     * revision {@code false} if the effective revision is to be just the revision of the change request without
     * merging.
     */
    boolean isMerge();
}
