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

package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;

public class MockChangeRequestSCMRevision extends ChangeRequestSCMRevision<MockChangeRequestSCMHead> {
    private final String hash;

    /**
     * Constructor.
     *
     * @param head   the {@link MockChangeRequestSCMHead} that the {@link SCMRevision} belongs to.
     * @param target the {@link SCMRevision} of the {@link MockChangeRequestSCMHead#getTarget()}.
     */
    public MockChangeRequestSCMRevision(
            @NonNull MockChangeRequestSCMHead head,
            @NonNull SCMRevision target, String hash) {
        super(head, target);
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockChangeRequestSCMRevision that = (MockChangeRequestSCMRevision) o;

        return hash.equals(that.hash);
    }

    @Override
    protected int _hashCode() {
        return hash.hashCode();
    }

    @Override
    public String toString() {
        return hash;
    }
}
