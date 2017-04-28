package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.scm.SCM;
import java.util.Arrays;
import java.util.Collection;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

public abstract class SCMBuilder<B extends SCMBuilder<B,S>,S extends SCM> {

    private final Class<S> clazz;
    private SCMHead head;
    private SCMRevision revision;

    public SCMBuilder(Class<S> clazz, @NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        this.clazz = clazz;
        this.head = head;
        this.revision = revision;
    }

    public Class<S> scmClass() {
        return clazz;
    }

    public SCMHead head() {
        return head;
    }

    public B withHead(@NonNull SCMHead head) {
        this.head = head;
        return (B) this;
    }

    public SCMRevision revision() {
        return revision;
    }

    public B withRevision(@CheckForNull SCMRevision revision) {
        this.revision = revision;
        return (B) this;
    }

    public abstract S build();

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        trait.applyToBuilder(this);
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
}
