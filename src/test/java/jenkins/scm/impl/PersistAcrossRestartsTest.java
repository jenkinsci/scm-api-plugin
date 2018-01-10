package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.FreeStyleProject;
import hudson.model.InvisibleAction;
import hudson.model.TaskListener;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.branch.BranchSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
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
                BranchSource branchSource = new BranchSource(source);
                WorkflowMultiBranchProject p = story.j.jenkins.createProject(WorkflowMultiBranchProject.class, "test-with-source");
                p.setSourcesList(Collections.singletonList(branchSource));

                // TODO: Find a better way to persist this for testing across restart.
                FreeStyleProject p2 = story.j.createFreeStyleProject("test-without-source");
                p2.setDescription(source.getId());
            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowMultiBranchProject p = (WorkflowMultiBranchProject) story.j.jenkins.getItem("test-with-source");
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
}
