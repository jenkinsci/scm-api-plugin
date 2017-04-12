package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;

public abstract class SCMHeadFilter {

    public abstract boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head);

}
