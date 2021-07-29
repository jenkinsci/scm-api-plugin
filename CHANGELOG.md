# SCM API Plugin Changelog

This plugin provides a new enhanced API for interacting with SCM systems.

## Version History

### Versions after 2.6.3 
Release notes for versions after v2.6.3 are tracked via [GitHub Releases](https://github.com/jenkinsci/scm-api-plugin/releases).

### Version 2.6.3 (July 11, 2019)

- Skipped version numbers due to release failure
- Push minimum Jenkins version back to 2.107.3

### Version 2.6.0 (July 8, 2019)

- [JENKINS-43802](https://issues.jenkins-ci.org/browse/JENKINS-43802) - Support Shared Library using folder-scoped credential

### Version 2.5.1 (Jun 20, 2019)

- Improved "exclusions by name" help text
- Fixed synchronization on termination of execution service

### Version 2.5.0 (Jun 17, 2019)

- Improve guessing nice names from URLs ([PR-64](https://github.com/jenkinsci/scm-api-plugin/pull/64))
- Use own executor service  to isolate long-running events from other work ([PR-61](https://github.com/jenkinsci/scm-api-plugin/pull/61))
- Jenkins 2.164.3 is the new required minimum version of Jenkins

### Version 2.4.0 (Mar 21, 2019)

- Java 8 and Jenkins 2.60.3 are the new required minimum versions.
- [PR-63](https://github.com/jenkinsci/scm-api-plugin/pull/63) Internal changes for better Java 11 compatibility

### Version 2.3.2 (Mar 21, 2019)

- Restore workaround for Jenkins Core Util.isOverridden until [JENKINS-56660](https://issues.jenkins-ci.org/browse/JENKINS-56660) is fixed

### Version 2.3.1 (Mar 21, 2019)

- [JENKINS-56656](https://issues.jenkins-ci.org/browse/JENKINS-56656) Enhance mock test harness to allow for testing change request trustability
- Various code tidy-ups [PR-60](https://github.com/jenkinsci/scm-api-plugin/pull/60) [PR-59](https://github.com/jenkinsci/scm-api-plugin/pull/59)
- Code tidy up identified bug in Jenkins Core. Do not use this release. Fix is in 2.3.2

### Version 2.3.0 (Oct 17, 2018)

- [JENKINS-52964](https://issues.jenkins.io/browse/JENKINS-52964) - add `SCMFileSystem.Builder#supports(SCMSourceDescriptor)` and `#supports(SCMDescriptor)` methods.

### Version 2.2.8 (Sept 28, 2018)

- Add traits-related methods to `SCMNavigator` and `SCMSource` base classes.
- Minor improvement to `WildcardSCMHeadFilterTrait` help documentation.

### Version 2.2.7 (Apr 20, 2018)

- Fix [JENKINS-50777](https://issues.jenkins-ci.org/browse/JENKINS-50777) exported API for SCMRevisionAction and SCMSource

### Version 2.2.6 (Dec 13, 2017)

- Enhanced the mock implementation test harness to allow testing SCMSource.afterSave() and SCMNavigator.afterSave() (no changes to user code)

### Version 2.2.5 (Nov 3, 2017)

- Add a utility API to allow implementation plugins to use a standard mechanism for normalizing SCM URIs in order to assist comparisons (no user visible changes)

### Version 2.2.4 (Nov 2, 2017)

- Clarify the help text for the built in filters.
  The built in filters treat the SCMHead name as a name, no special processing.
   A lot of people were wondering why when they set a filter like `master feature/*` why none of the PRs were building... the reason being that `PR-123` does not match the filter.
   When using the filters built in to SCM-API you probably want something like `master feature/* PR-*` to get PRs to build.
   An alternative filter implementation that probably does what you actually want is [SCM Filter Branch PR Plugin](https://plugins.jenkins.io/scm-filter-branch-pr) as that will filter non-change requests like the built in filters but has a special case for PRs where instead it filters those based on the PR target head rather than the name of the PR (which will always be PR-... unless the SCMSource uses a different prefix for its change request concept

### Version 2.2.3 (Oct 6, 2017)

- [JENKINS-47295](https://issues.jenkins-ci.org/browse/JENKINS-47295) Update to parent pom 2.36

### Version 2.2.2 (Sep 14, 2017)

Changes to test framework; no user-visible change.

Version 2.2.1 (Aug 22, 2017)

Changes to test framework; no user-visible change.

### Version 2.2.0 (Jul 17, 2017)

- [JENKINS-45436](https://issues.jenkins-ci.org/browse/JENKINS-45436) API to generate (mostly) human readable names of SCM server URLs
- [JENKINS-45434](https://issues.jenkins-ci.org/browse/JENKINS-45434) Add an avatar cache so that SCMs that providing fixed size avatars can convert to Jenkins native sizes
- [JENKINS-45331](https://issues.jenkins-ci.org/browse/JENKINS-45331) Prevent log-spam when instantiating trait based SCMNavigator / SCMSource implementations
- [JENKINS-43507](https://issues.jenkins-ci.org/browse/JENKINS-43507) Allow SCMSource and SCMNavigator subtypes to share common traits
- [JENKINS-44891](https://issues.jenkins-ci.org/browse/JENKINS-44891) Migrate SCMSource's id field to a @DataBoundSetter
- [JENKINS-44884](https://issues.jenkins-ci.org/browse/JENKINS-44884) Add @Symbol annotations
- [JENKINS-44648](https://issues.jenkins-ci.org/browse/JENKINS-44648) SCMRevisionAction should record corresponding source
- [JENKINS-43433](https://issues.jenkins-ci.org/browse/JENKINS-43433) Allow SCMSource implementations to expose merge and origin of change request heads

### Version 2.1.1 (Mar 16, 2017)

- [JENKINS-41736](https://issues.jenkins-ci.org/browse/JENKINS-41736) Add ability for SCMEvents to be contextually self-describing

### Version 2.1.0 (Mar 08, 2017)

- [JENKINS-42542](https://issues.jenkins-ci.org/browse/JENKINS-42542) `SCMHeadObserver.observe(SCMHead,SCMRevision)` should be allowed to throw IO and Interrupted exceptions

### Version 2.0.8 (Mar 01, 2017)

- **No user facing changes. The plugin is effectively identical to the 2.0.4 release.**
- Added `AbstractSampleRepoRule` & `AbstractSampleDVCSRepoRule` to the test harness.

### Version 2.0.7 (Feb 20, 2017)

- **No user facing changes. The plugin is effectively identical to the 2.0.4 release.**
- Test harness updates

### Version 2.0.6 (Feb 20, 2017)

- **No user facing changes. The plugin is effectively identical to the 2.0.4 release.**
- Test harness updates

### Version 2.0.5 (Feb 17, 2017)

- **No user facing changes. The plugin is effectively identical to the 2.0.4 release.**
- [JENKINS-42150](https://issues.jenkins-ci.org/browse/JENKINS-42150) update the test harness to allow the mock implementation to have fake latency
- [PR-28](https://github.com/jenkinsci/scm-api-plugin/pull/28) update the documentation for implementers of the SCM API
- [PR-31](https://github.com/jenkinsci/scm-api-plugin/pull/31) update the test dependencies to make running the plugin compatibility test suite against newer Jenkins versions easier

### Version 2.0.4 (Feb 14, 2017)

- [JENKINS-42000](https://issues.jenkins-ci.org/browse/JENKINS-42000) API change to enable fix of JENKINS-42000

### Version 2.0.3 (Feb 7, 2017)

- [JENKINS-41795](https://issues.jenkins-ci.org/browse/JENKINS-41795) Add a way to track the origin of SCM Events

### Version 2.0.2 (Feb 2, 2017)

- [JENKINS-41453](https://issues.jenkins-ci.org/browse/JENKINS-41453)
- [JENKINS-41121](https://issues.jenkins-ci.org/browse/JENKINS-41121)
- Scare admins away from doing partial updates
- Need to be able to recover a controller with a specific ID for LocalData tests
- Make MockSCM usable from Pipeline's checkout step
- [JENKINS-40828](https://issues.jenkins-ci.org/browse/JENKINS-40828) Fix some NPEs found when using downstream
- Tags need a timestamp
- [JENKINS-38718](https://issues.jenkins-ci.org/browse/JENKINS-38718)
- [JENKINS-40829](https://issues.jenkins-ci.org/browse/JENKINS-40829)
- [JENKINS-40828](https://issues.jenkins-ci.org/browse/JENKINS-40828)
- [JENKINS-40827](https://issues.jenkins-ci.org/browse/JENKINS-40827)
- Javadoc errors to zero
- Merge pull request \#19 from jenkinsci/navigator-ids
- Merge pull request \#18 from jenkinsci/important-doc-on-ids
- Noting correct issue
- Fix namespace
- Add utilities to assist with event related testing
- Simplify the child creation
- NPE from findbugs
- towards 2.0.1
- Signature should be \`of(Item,SCM)\` not \`of(SCM)\`
- SPI should not call back to API
- EventListeners expect to be ACL.SYSTEM when notified

### Version 2.0.1 (Jan 16, 2016)

This release caused [JENKINS-41121](https://issues.jenkins-ci.org/browse/JENKINS-41121).

- Please read [this Blog Post](https://jenkins.io/blog/2017/01/17/scm-api-2/) before upgrading

### Version 2.0.1-beta-1 (Dec 16, 2016)

- Released to experimental update center to allow testing the downstream changes in github-branch-source and bitbucket-branch-source (both of which need upgrading to at least 2.0.0-beta-1 if you have them installed on your master)

### Version 2.0 (Dec 7, 2016)

- [JENKINS-38987](https://issues.jenkins-ci.org/browse/JENKINS-38987)
    Added pronouns to assist consuming plugins to name concepts like:
    SCMHead; SCMSource; and SCMNavigator with SCM specific idiomatic
    names, e.g. GitHub can respectively provide pronouns
    "Branch"/"Tag"/"Pull request"; "Repository"; "Server", whereas
    something like Accurev could provide pronouns "Stream"/"Snapshot";
    "Depot"; "Repository". The pronouns are more relevant for SCMHead as
    typically each SCM implementation is likely to have more than one
    type of head: mainlines, branches, tags, change requests, etc
- [JENKINS-39355](https://issues.jenkins-ci.org/browse/JENKINS-38355) Various
    API improvements that make it easier to implement/consume SCM API
    including the addition of an event system to allow SCM
    implementations to consolidate push event handling from their
    backing SCM server.
- [JENKINS-40138](https://issues.jenkins-ci.org/browse/JENKINS-40138) SCMHead.getActions()
    should never have been introduced into the SCM API. JENKINS-33309
    was a mistake. The API is now marked as DoNotUse and is
    non-functional. Replacement API for the correct way to access the
    corresponding information have been documented.
- Added documentation for [plugin authors](https://github.com/jenkinsci/scm-api-plugin/blob/master/docs/implementation.adoc)
  who are implementing the SCM API for their SCM system and
  [consumers](https://github.com/jenkinsci/scm-api-plugin/blob/master/docs/consumer.adoc)
  of the SCM API that want to access different SCM implementations through a single generic API
- Added a [mock SCM implementation](https://github.com/jenkinsci/scm-api-plugin/tree/master/src/test/java/jenkins/scm/impl/mock) in
    the tests-jar so that consumers can write unit tests of their
    implementation without needing to create SCM servers and manipulate
    SCM repositories to generate events or test conditions.

### Version 1.3 (Sep 7, 2016)

- Infrastructure for [JENKINS-31155](https://issues.jenkins-ci.org/browse/JENKINS-31155).
- [JENKINS-32768](https://issues.jenkins-ci.org/browse/JENKINS-32768) `SingleSCMSource` configuration was not properly round-tripped.
- More emphatically discourage use of `SingleSCMSource`.

### Version 1.2 (Apr 11, 2016)

- [JENKINS-33808](https://issues.jenkins-ci.org/browse/JENKINS-33808)
    Support for Item categorization. More information about this new
    feature in core here
    [JENKINS-31162](https://issues.jenkins-ci.org/browse/JENKINS-31162)

### Version 1.1 (Mar 10, 2016)

- [JENKINS-33256](https://issues.jenkins-ci.org/browse/JENKINS-33256) `SCMSource.getTrustedRevision` API.
- [JENKINS-33309](https://issues.jenkins-ci.org/browse/JENKINS-33309) `SCMHead` may have actions, such as `ChangeRequestAction`.

### Version 1.0 (Nov 12, 2015)

- [JENKINS-30595](https://issues.jenkins-ci.org/browse/JENKINS-30595) `HeadByItem` API.

### Version 0.3-beta-1

- Introduced `SCMNavigator` API.
- [JENKINS-21007](https://issues.jenkins-ci.org/browse/JENKINS-21007) Add a mechanism to get parent revision.

### Version 0.2

Changelog not recorded.

### Version 0.1

Initial release.
