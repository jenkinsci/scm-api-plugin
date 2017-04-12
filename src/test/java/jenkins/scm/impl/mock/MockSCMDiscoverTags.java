package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceRequestBuilder;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.TagSCMHeadCategory;
import org.kohsuke.stapler.DataBoundConstructor;

public class MockSCMDiscoverTags extends SCMSourceTrait {

    @DataBoundConstructor
    public MockSCMDiscoverTags() {
    }

    @Override
    protected <B extends SCMSourceRequestBuilder<B, R>, R extends SCMSourceRequest> void decorateRequest(B builder) {
        if (builder instanceof MockSCMSourceRequestBuilder) {
            ((MockSCMSourceRequestBuilder) builder).withTags(true);
        }
    }

    @Override
    public boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        return category instanceof TagSCMHeadCategory;
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
