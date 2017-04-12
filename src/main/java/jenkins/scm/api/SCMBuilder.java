package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import java.util.Arrays;
import java.util.Collection;
import jenkins.model.Jenkins;
import jenkins.scm.api.trait.SCMSourceTrait;

public abstract class SCMBuilder<B extends SCMBuilder<B,S>,S extends SCM> {

    private final Class<S> clazz;
    private final SCMHead head;
    private final SCMRevision revision;

    public SCMBuilder(Class<S> clazz, @NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        this.clazz = clazz;
        this.head = head;
        this.revision = revision;
    }

    public SCMHead getHead() {
        return head;
    }

    public SCMRevision getRevision() {
        return revision;
    }

    public abstract S build();

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        trait.applyToSCM((B)this);
        return (B) this;
    }

    public B withTraits(@NonNull SCMSourceTrait... traits) {
        return withTraits(Arrays.asList(traits));
    }

    @SuppressWarnings("unchecked")
    public B withTraits(@NonNull Collection<SCMSourceTrait> traits) {
        for (SCMSourceTrait trait : traits) {
            withTrait(trait);
        }
        return (B) this;
    }

    public SCMDescriptor<S> getSCMDescriptor() {
        return (SCMDescriptor<S>)Jenkins.getActiveInstance().getDescriptorOrDie(clazz);
    }
}
