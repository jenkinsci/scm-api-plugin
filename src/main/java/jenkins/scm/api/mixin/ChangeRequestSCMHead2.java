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

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

/**
 * Additionl attributes of a {@link ChangeRequestSCMHead} that should have been in the original mixin but we are not
 * targeting Java 8 so we cannot add the default methods to the interface and must have an ugly {@code 2} class instead.
 * @since 2.2.0
 */
// TODO once Java 8 is baseline move method to ChangeRequestSCMHead with default return value,
// TODO deprecate this interface and add @Restricted(NoExternalUse.class) (retain empty interface for binary compat)
public interface ChangeRequestSCMHead2 extends ChangeRequestSCMHead {
    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} of this {@link ChangeRequestSCMHead}.
     *
     * @return the {@link ChangeRequestCheckoutStrategy}.
     */
    @NonNull
    ChangeRequestCheckoutStrategy getCheckoutStrategy();

}
