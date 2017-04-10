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

package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Locale;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;

public class MockChangeRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {
    private final String target;
    private final Integer number;
    private final SCMHeadOrigin origin;
    private final ChangeRequestCheckoutStrategy strategy;

    public MockChangeRequestSCMHead(SCMHeadOrigin origin, Integer number, String target,
                                    ChangeRequestCheckoutStrategy strategy, boolean singleStrategy) {
        super("CR-"+number + (singleStrategy ? "" : "-"+strategy.name().toLowerCase(Locale.ENGLISH)));
        this.number = number;
        this.target = target;
        this.origin = origin;
        this.strategy = ChangeRequestCheckoutStrategy.HEAD;
    }

    public MockChangeRequestSCMHead(Integer number, String target) {
        this(null, number, target, ChangeRequestCheckoutStrategy.HEAD, true);
    }

    @NonNull
    @Override
    public String getId() {
        return number.toString();
    }

    @NonNull
    @Override
    public SCMHead getTarget() {
        return new MockSCMHead(target);
    }

    public Integer getNumber() {
        return number;
    }

    @NonNull
    @Override
    public SCMHeadOrigin getOrigin() {
        return origin == null ? SCMHeadOrigin.DEFAULT : origin;
    }

    @NonNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return strategy;
    }
}
