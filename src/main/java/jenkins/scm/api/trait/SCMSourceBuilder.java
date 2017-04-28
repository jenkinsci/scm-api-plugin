package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.scm.SCM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

public abstract class SCMSourceBuilder<B extends SCMSourceBuilder<B,S>,S extends SCMSource> {

    private final Class<S> clazz;
    private final String projectName;
    private final List<SCMSourceTrait> traits = new ArrayList<SCMSourceTrait>();

    public SCMSourceBuilder(Class<S> clazz, String projectName) {
        this.clazz = clazz;
        this.projectName = projectName;
    }

    public Class<S> sourceClass() {
        return clazz;
    }

    public String projectName() {
        return projectName;
    }

    public abstract S build();

    public List<SCMSourceTrait> traits() {
        return new ArrayList<SCMSourceTrait>(traits);
    }

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMNavigatorTrait trait) {
        trait.applyToBuilder(this);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        traits.add(trait);
        return (B) this;
    }

    public B withTraits(@NonNull SCMTrait<? extends SCMTrait<?>>... traits) {
        return withTraits(Arrays.asList(traits));
    }
    @SuppressWarnings("unchecked")
    public B withTraits(@NonNull Collection<? extends SCMTrait<?>> traits) {
        for (SCMTrait<?> trait : traits) {
            if (trait instanceof SCMNavigatorTrait) {
                withTrait((SCMNavigatorTrait)trait);
            } else if (trait instanceof SCMSourceTrait) {
                withTrait((SCMSourceTrait) trait);
            } else {
                throw new IllegalArgumentException("Unsupported trait: " + trait.getClass().getName());
            }
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withRequest(SCMNavigatorRequest request) {
        withTraits(request.traits());
        for (SCMSourceDecorator<?,?> decorator: request.decorators()) {
            decorator.applyTo(this, projectName());
        }
        return (B) this;
    }
}
