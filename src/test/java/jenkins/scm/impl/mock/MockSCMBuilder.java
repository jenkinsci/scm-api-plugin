package jenkins.scm.impl.mock;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMBuilder;
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
        SCMRevision revision = getRevision();
        return new MockSCM(source, getHead(),
                revision instanceof MockSCMRevision || revision instanceof MockChangeRequestSCMRevision
                        ? revision
                        : null);
    }

}
