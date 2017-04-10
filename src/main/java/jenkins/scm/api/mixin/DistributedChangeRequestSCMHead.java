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
 * A change request that may originate from multiple sources.
 * <ul>
 * <li>A centralized version control system, such as Subversion would not have this type of change request.</li>
 * <li>Distributed version control systems, such as Git and Mercurial could have this concept as a change
 * request could be from a different repository.</li>
 * <li>Centralized distributed version control systems, such as GitHub and Bitbucket have this concept as
 * a change request can originate from either the origin repository or a fork of the origin repository.
 * </li>
 * </ul>
 *
 * @since 2.2.0
 */
public interface DistributedChangeRequestSCMHead extends ChangeRequestSCMHead {
    /**
     * Is this a {@link DistributedChangeRequestSCMHead} from the "origin"?
     *
     * @return {@code true} if the {@link DistributedChangeRequestSCMHead} is from the "origin", {@code false}
     * if from another "source".
     */
    boolean isFromOrigin();
}
