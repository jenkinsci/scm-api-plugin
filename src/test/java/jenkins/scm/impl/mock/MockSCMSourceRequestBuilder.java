package jenkins.scm.impl.mock;

import hudson.model.TaskListener;
import java.util.EnumSet;
import java.util.Set;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceRequestBuilder;

public class MockSCMSourceRequestBuilder extends SCMSourceRequestBuilder<MockSCMSourceRequestBuilder, MockSCMSourceRequest> {

    private boolean needsBranches;
    private boolean needsTags;
    private boolean needsChangeRequests;
    private Set<ChangeRequestCheckoutStrategy> checkoutStrategies = EnumSet.noneOf(ChangeRequestCheckoutStrategy.class);

    public MockSCMSourceRequestBuilder(SCMSourceCriteria criteria, SCMHeadObserver observer, TaskListener listener) {
        super(criteria, observer, listener);
    }

    public MockSCMSourceRequestBuilder withBranches(boolean included) {
        needsBranches = needsBranches || included;
        return this;
    }

    public MockSCMSourceRequestBuilder withTags(boolean included) {
        needsTags = needsTags || included;
        return this;
    }

    public MockSCMSourceRequestBuilder withChangeRequests(boolean included) {
        needsChangeRequests = needsChangeRequests || included;
        return this;
    }

    public boolean needsBranches() {
        return needsBranches;
    }

    public boolean needsTags() {
        return needsTags;
    }

    public boolean needsChangeRequests() {
        return needsChangeRequests;
    }

    public Set<ChangeRequestCheckoutStrategy> checkoutStrategies() {
        return checkoutStrategies;
    }

    public MockSCMSourceRequestBuilder withCheckoutStrategies(Set<ChangeRequestCheckoutStrategy> checkoutStrategies) {
        this.checkoutStrategies.addAll(checkoutStrategies);
        return this;
    }

    @Override
    public MockSCMSourceRequest build() {
        return new MockSCMSourceRequest(this);
    }
}
