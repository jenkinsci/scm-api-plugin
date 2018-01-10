package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class PersistAcrossRestartsTest {
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Issue("JENKINS-48571")
    @Test
    public void uuidIdPersists() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                BogusSCMSource source = new BogusSCMSource();
                SCMItem p = story.j.jenkins.createProject(SCMItem.class, "test-with-source");
                p.setSource(source);
                // TODO: Find a better way to persist this for testing across restart.
                FreeStyleProject p2 = story.j.createFreeStyleProject("test-without-source");
                p2.setDescription(source.getId());
            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                SCMItem p = (SCMItem) story.j.jenkins.getItem("test-with-source");
                FreeStyleProject p2 = (FreeStyleProject)story.j.jenkins.getItem("test-without-source");
                assertNotNull(p);
                assertNotNull(p2);
                SCMSource source = p.getSCMSource(p2.getDescription());
                assertNotNull(source);
                assertFalse(source instanceof NullSCMSource);
            }
        });
    }

    public static class BogusSCMSource extends SCMSource {
        /**
         * Constructor.
         */
        public BogusSCMSource() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer,
                                @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener)
                throws IOException, InterruptedException {

        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public SCM build(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
            return new NullSCM();
        }
    }

    public static final class SCMItem extends Job<SCMItem,SCMRun> implements TopLevelItem, SCMSourceOwner {

        public SCMSource source;

        public SCMItem(ItemGroup parent, String name) {
            super(parent, name);
        }

        public void setSource(SCMSource source) {
            source.setOwner(this);
            this.source = source;
        }

        @Override
        public boolean isBuildable() {
            return false;
        }

        @Override
        public void removeRun(SCMRun r) {

        }

        @Override
        protected SortedMap<Integer, ? extends SCMRun> _getRuns() {
            return new TreeMap<>();
        }

        @Override
        public List<SCMSource> getSCMSources() {
            if (source != null) {
                return Collections.singletonList(source);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
            return null;
        }

        @Override
        public SCMSource getSCMSource(String sourceId) {
            if (source != null && source.getId().equals(sourceId)) {
                return source;
            } else {
                return null;
            }
        }

        @Override
        @Deprecated
        public void onSCMSourceUpdated(@NonNull SCMSource source) {}


        @Override
        public TopLevelItemDescriptor getDescriptor() {
            return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
        }

        @TestExtension
        public static final class DescriptorImpl extends TopLevelItemDescriptor {

            @Override public TopLevelItem newInstance(ItemGroup parent, String name) {
                return new SCMItem(parent, name);
            }

        }
    }

    public static final class SCMRun extends Run<SCMItem,SCMRun> {
        public SCMRun(SCMItem j) throws IOException {
            super(j);
        }
    }

}
