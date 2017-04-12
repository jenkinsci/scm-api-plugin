package jenkins.scm.impl.mock;

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

    MockSCMSourceRequest(MockSCMSourceRequestBuilder builder) {
        super(builder);
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
