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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

public class MockSCMBuilder extends SCMBuilder<MockSCMBuilder,MockSCM> {

    private final MockSCMSource source;

    public MockSCMBuilder(@NonNull MockSCMSource source, @NonNull SCMHead head,
                          @CheckForNull SCMRevision revision) {
        super(MockSCM.class, head, revision);
        this.source = source;
    }

    @Override
    public MockSCM build() {
        SCMRevision revision = revision();
        return new MockSCM(source, head(),
                revision instanceof MockSCMRevision || revision instanceof MockChangeRequestSCMRevision
                        ? revision
                        : null);
    }

}
