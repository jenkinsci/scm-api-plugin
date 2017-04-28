package jenkins.scm.api.trait;

import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;

public class SCMTrait<T extends SCMTrait<T>> extends AbstractDescribableImpl<T> {
    /**
     * {@inheritDoc}
     */
    @Override
    public SCMTraitDescriptor<T> getDescriptor() {
        return (SCMTraitDescriptor<T>) super.getDescriptor();
    }

    public static <T extends SCMTrait<T>,D extends SCMTraitDescriptor<T>> DescriptorExtensionList<T, D> all(Class<T> type) {
        return Jenkins.getActiveInstance().getDescriptorList(type);
    }

}
