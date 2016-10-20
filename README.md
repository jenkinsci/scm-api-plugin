# Jenkins SCM API Plugin

 This plugin provides a new enhanced API for interacting with SCM systems. See also this [plugin's wiki page][wiki]

# Environment

The following build environment is required to build this plugin

* `java-1.6` and `maven-3.0.5`

# Build

To build the plugin locally:

    mvn clean verify

# Release

To release the plugin:

    mvn release:prepare release:perform -B

# Test local instance

To test in a local Jenkins instance

    mvn hpi:run

  [wiki]: http://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin

# SCM API Data Model

## SCMSource

[SCMSource](src/main/java/jenkins/scm/api/SCMSource.java)
[(doc)](http://javadoc.jenkins.io/plugin/scm-api/jenkins/scm/api/SCMSource.html)
implementations provide a configuration form (as a
[Descriptor](http://javadoc.jenkins-ci.org/hudson/model/Descriptor.html))
for users, and define the methods for multi-branch and other advanced
SCM concepts in Jenkins.

The most crucial methods an
[SCMSource](src/main/java/jenkins/scm/api/SCMSource.java) must
implement are:

* [retrieve(HeadObserver, TaskListener)](https://github.com/jenkinsci/scm-api-plugin/blob/c06684f6e252bd4f8ef99ab8ec6eefceed8dce45/src/main/java/jenkins/scm/api/SCMSource.java#L153) - provides HeadObserver with the Set(name, hash) of revisions to consider
* [retrieve(SCMHead, TaskListener)](https://github.com/MarkEWaite/scm-api-plugin/blob/c06684f6e252bd4f8ef99ab8ec6eefceed8dce45/src/main/java/jenkins/scm/api/SCMSource.java#L222) - returns a single revision
* [retrieve(String, TaskListener)](https://github.com/MarkEWaite/scm-api-plugin/blob/c06684f6e252bd4f8ef99ab8ec6eefceed8dce45/src/main/java/jenkins/scm/api/SCMSource.java#L282) - returns a single revision identified by the string value

## SCMHead

[SCMHead](src/main/java/jenkins/scm/api/SCMHead.java)
[(doc)](http://javadoc.jenkins.io/plugin/scm-api/jenkins/scm/api/SCMHead.html)
is a named tree (subversion branch, mercurial branch, git branch,
perforce branch/stream, etc.), identified by name

## SCMRevision

[SCMRevision](src/main/java/jenkins/scm/api/SCMRevision.java)
[(doc)](http://javadoc.jenkins.io/plugin/scm-api/jenkins/scm/api/SCMRevision.html)
is a specific change in the tree (subversion revision, mercurial hash,
git sha, perforce change, etc.) identified by a version control
specific object.

## SCMStatus

SCMStatus implementations (like
[mercurial](https://github.com/jenkinsci/mercurial-plugin/blob/master/src/main/java/hudson/plugins/mercurial/MercurialStatus.java),
[git](https://github.com/jenkinsci/git-plugin/blob/master/src/main/java/hudson/plugins/git/GitStatus.java),
[subversion](https://github.com/jenkinsci/subversion-plugin/blob/master/src/main/java/hudson/scm/SubversionStatus.java),
[perforce](https://github.com/jenkinsci/p4-plugin/blob/master/src/main/java/org/jenkinsci/plugins/p4/trigger/P4Hook.java))
provide an [unprotected root action](http://javadoc.jenkins.io/hudson/model/UnprotectedRootAction.html) which is used to notify Jenkins
that it should poll a repository for changes.  Refer to Kohsuke's 2011 blog posting,
"[polling must die](http://kohsuke.org/2011/12/01/polling-must-die-triggering-jenkins-builds-from-a-git-hook/)"
for the ways that users benefit from an SCM implementation that provides an SCMStatus URL.

# SCM Data Flow

The [build(SCMHead, SCMRevision)](https://github.com/MarkEWaite/scm-api-plugin/blob/c06684f6e252bd4f8ef99ab8ec6eefceed8dce45/src/main/java/jenkins/scm/api/SCMSource.java#L348)
[(doc)](http://javadoc.jenkins.io/scm-api/jenkins/scm/api/SCMSource.html#build-jenkins.scm.api.SCMHead-jenkins.scm.api.SCMRevision)
method returns a reference to your SCM implementation as constructed
from user provided values (e.g. jelly provided values).  Refer to
[mercurial](https://github.com/jenkinsci/mercurial-plugin/blob/4e9fcd772cd4a4c1264e19a175476b5e48cd1c94/src/main/java/hudson/plugins/mercurial/MercurialSCMSource.java#L142) as a reference implementation.

The [getCriteria()](https://github.com/jenkinsci/scm-api-plugin/blob/c06684f6e252bd4f8ef99ab8ec6eefceed8dce45/src/main/java/jenkins/scm/api/SCMSource.java#L125)
[(doc)](http://javadoc.jenkins.io/scm-api/jenkins/scm/api/SCMSource.html#getCriteria)
method provides criteria (such as the presence of a Jenkinsfile in the
root of the repository branch) used to select which branches should
have jobs created for them.  Refer to
[AbstractGitSCMSource](https://github.com/jenkinsci/git-plugin/blob/1b555c8d995413160f3eea80b3f6926e7b411369/src/main/java/jenkins/plugins/git/AbstractGitSCMSource.java#L313)
as the reference implementation
