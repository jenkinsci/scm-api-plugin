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
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceRequestBuilder;
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
    protected <B extends SCMSourceRequestBuilder<B, R>, R extends SCMSourceRequest> void decorateRequest(B builder) {
        if (builder instanceof MockSCMSourceRequestBuilder) {
            ((MockSCMSourceRequestBuilder) builder).withChangeRequests(true);
            ((MockSCMSourceRequestBuilder) builder).withCheckoutStrategies(strategies);
        }
    }

    @Override
    public boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        return category instanceof ChangeRequestSCMHeadCategory;
    }

    @Extension
    public static final class DescriptorImpl extends SCMSourceTraitDescriptor {

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public boolean isApplicableTo(Class<? extends SCMSourceRequestBuilder> requestBuilderClass) {
            return MockSCMSourceRequestBuilder.class.isAssignableFrom(requestBuilderClass);
        }

        @Override
        public boolean isApplicableTo(SCMDescriptor<?> scm) {
            return scm instanceof MockSCM.DescriptorImpl;
        }
    }
}
