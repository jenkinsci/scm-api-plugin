/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

import hudson.model.TaskListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceRequest;

public class MockSCMSourceRequest extends SCMSourceRequest {
    private final boolean fetchBranches;
    private final boolean fetchTags;
    private final boolean fetchChangeRequests;
    private final Set<ChangeRequestCheckoutStrategy> checkoutStrategies;

    MockSCMSourceRequest(MockSCMSourceRequestBuilder builder, TaskListener listener) {
        super(builder, listener);
        this.fetchBranches = builder.needsBranches();
        this.fetchTags = builder.needsTags();
        this.fetchChangeRequests = builder.needsChangeRequests();
        this.checkoutStrategies = builder.checkoutStrategies().isEmpty()
                ? Collections.<ChangeRequestCheckoutStrategy>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(builder.checkoutStrategies()));
    }

    public boolean isFetchBranches() {
        return fetchBranches;
    }

    public boolean isFetchTags() {
        return fetchTags;
    }

    public boolean isFetchChangeRequests() {
        return fetchChangeRequests;
    }

    public Set<ChangeRequestCheckoutStrategy> getCheckoutStrategies() {
        return checkoutStrategies;
    }
}
