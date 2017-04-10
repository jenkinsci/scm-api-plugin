package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;

/**
 * @author Stephen Connolly
 */
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
