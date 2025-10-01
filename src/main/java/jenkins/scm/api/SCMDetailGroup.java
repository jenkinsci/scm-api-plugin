package jenkins.scm.api;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.details.DetailGroup;

/**
 * A {@link DetailGroup} implementation that provides SCM-related details.
 */
@Extension(ordinal = -1)
public class SCMDetailGroup extends DetailGroup {

    public static SCMDetailGroup get() {
        return ExtensionList.lookupSingleton(SCMDetailGroup.class);
    }
}
