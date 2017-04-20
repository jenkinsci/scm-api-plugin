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

/**
 * The various strategies that can be used when checking out a change request.
 *
 * @since 2.2.0
 */
public enum ChangeRequestCheckoutStrategy {
    /**
     * The revision to be checked out will be independent of the revision of the
     * {@link ChangeRequestSCMHead#getTarget()}. For example, with GitHub, Bitbucket, etc this would correspond to
     * checking out the Head revision of the PR.
     */
    HEAD,
    /**
     * The revision to be checked out will be the result of applying a merge algorithm between the the revision
     * of the change request and the revision of the {@link ChangeRequestSCMHead#getTarget()}.
     * <p>
     * In the event that the merge operation cannot be completed then the checkout operation will fail.
     * </p>
     * <p>
     * It is assumed that the algorithm for merging is deterministic given the revision of the change request and
     * the revision of the {@link ChangeRequestSCMHead#getTarget()}
     * </p>
     */
    MERGE;
}
