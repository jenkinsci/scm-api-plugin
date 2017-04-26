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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.scm.SCMDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class MockSCMDiscoverChangeRequests extends SCMSourceTrait {

    private final Set<ChangeRequestCheckoutStrategy> strategies;

    public MockSCMDiscoverChangeRequests(Collection<ChangeRequestCheckoutStrategy> strategies) {
        this.strategies =
                strategies.isEmpty() ? EnumSet.of(ChangeRequestCheckoutStrategy.HEAD) : EnumSet.copyOf(strategies);
    }

    public MockSCMDiscoverChangeRequests(ChangeRequestCheckoutStrategy... strategies) {
        this(Arrays.asList(strategies));
    }

    @DataBoundConstructor
    public MockSCMDiscoverChangeRequests(String strategiesStr) {
        this(fromString(strategiesStr));
    }

    private static Set<ChangeRequestCheckoutStrategy> fromString(String strategiesStr) {
        Set<ChangeRequestCheckoutStrategy> strategies = EnumSet.noneOf(ChangeRequestCheckoutStrategy.class);
        for (String s : StringUtils.split(strategiesStr, ", ")) {
            try {
                strategies.add(ChangeRequestCheckoutStrategy.valueOf(s.trim()));
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return strategies;
    }

    public String getStrategiesStr() {
        StringBuilder r = new StringBuilder();
        for (ChangeRequestCheckoutStrategy s : strategies) {
            r.append(s.name()).append(", ");
        }
        return r.toString();
    }


    @Override
    protected <B extends SCMSourceContext<B, R>, R extends SCMSourceRequest> void decorateContext(B context) {
        if (context instanceof MockSCMSourceContext) {
            ((MockSCMSourceContext) context).withChangeRequests(true);
            ((MockSCMSourceContext) context).withCheckoutStrategies(strategies);
        }
    }

    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category instanceof ChangeRequestSCMHeadCategory;
    }

    @Extension
    public static final class DescriptorImpl extends SCMSourceTraitDescriptor {

        @Override
        public String getDisplayName() {
            return "Discover change requests";
        }

        @Override
        public boolean isApplicableTo(Class<? extends SCMSourceContext> builderClass) {
            return MockSCMSourceContext.class.isAssignableFrom(builderClass);
        }

        @Override
        public boolean isApplicableTo(SCMDescriptor<?> scm) {
            return scm instanceof MockSCM.DescriptorImpl;
        }
    }
}
