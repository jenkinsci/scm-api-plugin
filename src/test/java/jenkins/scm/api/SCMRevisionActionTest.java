package jenkins.scm.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import hudson.model.Action;
import hudson.model.Actionable;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.jupiter.api.Test;

class SCMRevisionActionTest {

    @Test
    void given__legacyData__when__gettingRevision__then__legacyReturned() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMSource source = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            SCMRevision revision = new MockSCMRevision(new MockSCMHead("head"), "hash");
            Actionable a = new ActionableImpl();
            a.addAction(new SCMRevisionAction(revision, null));
            assertThat(SCMRevisionAction.getRevision(source, a), is(revision));
        }
    }

    @Test
    void given__mixedData__when__gettingRevision__then__legacyReturnedForUnmatched() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMSource source1 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source1.setId("foo");
            MockSCMSource source2 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source2.setId("bar");
            MockSCMSource source3 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source3.setId("manchu");
            SCMRevision revision1 = new MockSCMRevision(new MockSCMHead("head1"), "hash1");
            SCMRevision revision2 = new MockSCMRevision(new MockSCMHead("head2"), "hash2");
            SCMRevision revision3 = new MockSCMRevision(new MockSCMHead("head3"), "hash3");
            Actionable a = new ActionableImpl();
            a.addAction(new SCMRevisionAction(source1, revision1));
            a.addAction(new SCMRevisionAction(revision2, null));
            a.addAction(new SCMRevisionAction(source3, revision3));
            assertThat(SCMRevisionAction.getRevision(source1, a), is(revision1));
            assertThat(SCMRevisionAction.getRevision(source2, a), is(revision2));
            assertThat(SCMRevisionAction.getRevision(source3, a), is(revision3));
        }
    }

    @Test
    void given__mixedData__when__gettingRevision__then__firstlegacyReturnedForUnmatched() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMSource source1 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source1.setId("foo");
            MockSCMSource source2 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source2.setId("bar");
            MockSCMSource source3 = new MockSCMSource(c, "test", new MockSCMDiscoverBranches());
            source3.setId("manchu");
            SCMRevision revision1 = new MockSCMRevision(new MockSCMHead("head1"), "hash1");
            SCMRevision revision2 = new MockSCMRevision(new MockSCMHead("head2"), "hash2");
            SCMRevision revision3 = new MockSCMRevision(new MockSCMHead("head3"), "hash3");
            Actionable a = new ActionableImpl();
            a.addAction(new SCMRevisionAction(source1, revision1));
            a.addAction(new SCMRevisionAction(revision2, null));
            a.addAction(new SCMRevisionAction(revision3, null));
            assertThat(SCMRevisionAction.getRevision(source1, a), is(revision1));
            assertThat(SCMRevisionAction.getRevision(source2, a), is(revision2));
            assertThat("Cannot distinguish legacy", SCMRevisionAction.getRevision(source3, a), is(revision2));
        }
    }

    private static class ActionableImpl extends Actionable {
        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getSearchUrl() {
            return null;
        }

        @SuppressWarnings("deprecation") // avoid TransientActionFactory
        @Override
        public <T extends Action> List<T> getActions(Class<T> type) {
            return getActions().stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }
    }
}
