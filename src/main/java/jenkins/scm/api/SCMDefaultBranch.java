package jenkins.scm.api;

import hudson.ExtensionPoint;

/**
 * Allow implementers to nominate a default "branch"
 *
 * Both github and bitbucket have the concept of a master/default branch for a repo.
 * Generally in git this is by convention (at the time of writing).
 *
 *
 * @author Michael Neale
 */
public abstract class SCMDefaultBranch implements ExtensionPoint {

    public abstract String getDefaultBranch(SCMSource scm);

}
