/*
 * The MIT License
 *
 * Copyright (c) 2016-2017 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package jenkins.scm.impl.mock;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.PollingResult;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jenkins.scm.api.SCM2;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.xml.sax.SAXException;

public class MockSCM extends SCM2 implements Serializable {
    private final String controllerId;
    private final String repository;
    private final SCMHead head;
    private final SCMRevision revision;
    private transient MockSCMController controller;

    @DataBoundConstructor
    public MockSCM(String controllerId, String repository, String head, String revision) {
        this.controllerId = controllerId;
        this.repository = repository;
        // implementations of SCM API need not use the same convention for SCMHead.getName() as their underlying
        // SCM does, e.g. GitHub calls pull requests PR-# or PR-#-head or PR-#-merge
        // we simulate this here in order to ensure that the branch api has not made any implicit assumptions
        // though it would be simpler to not have a unified namespace for tags and branches in MockSCMController
        // and to have MockSCMController use a separate set of API methods in checking out change requests
        // instead of merging them into the MockSCMController namespace under change-request/#
        Matcher m = Pattern.compile("CR-(\\d+)(-[a-zA-Z]+)?").matcher(head);
        if (m.matches()) {
            int number = Integer.parseInt(m.group(1));
            String target = null;
            String targetRevision = null;
            Set<MockChangeRequestFlags> flags = null;
            try {
                target = controller().getTarget(repository, number);
                targetRevision = controller().getRevision(repository, target);
                flags = controller().getFlags(repository, number);
            } catch (IOException e) {
                // ignore
            }
            ChangeRequestCheckoutStrategy strategy = ChangeRequestCheckoutStrategy.HEAD;
            String strategyStr = m.group(2);
            if (StringUtils.isNotBlank(strategyStr)) {
                for (ChangeRequestCheckoutStrategy s : ChangeRequestCheckoutStrategy.values()) {
                    if (strategyStr.equals("-"+(s.name().toLowerCase(Locale.ENGLISH)))) {
                        strategy = s;
                        break;
                    }
                }
            }
            MockChangeRequestSCMHead h = new MockChangeRequestSCMHead(
                    flags == null || !flags.contains(MockChangeRequestFlags.FORK)
                            ? SCMHeadOrigin.DEFAULT
                            : new SCMHeadOrigin.Fork("fork"),
                    number,
                    target,
                    strategy,
                    StringUtils.isNotBlank(strategyStr)
            );
            this.head = h;
            this.revision = new MockChangeRequestSCMRevision(h,
                    new MockSCMRevision(h.getTarget(), targetRevision), revision);
        } else if (head.startsWith("TAG:")) {
            long timestamp = 0;
            try {
                timestamp = controller().getTagTimestamp(repository, head.substring(4));
            } catch (IOException e) {
                // ignore
            }
            this.head = new MockTagSCMHead(head.substring(4), timestamp);
            this.revision = revision == null ? null : new MockSCMRevision(this.head, revision);
        } else {
            this.head = new MockSCMHead(head);
            this.revision = revision == null ? null : new MockSCMRevision(this.head, revision);
        }
    }

    public MockSCM(MockSCMSource config, SCMHead head, SCMRevision revision) {
        this.controllerId = config.getControllerId();
        this.repository = config.getRepository();
        this.head = head;
        this.revision = revision;
    }

    public MockSCM(MockSCMController controller, String repository, SCMHead head, SCMRevision revision) {
        this.controllerId = controller.getId();
        this.controller = controller;
        this.repository = repository;
        this.revision = revision;
        this.head = head;
    }

    public String getControllerId() {
        return controllerId;
    }

    private MockSCMController controller() {
        if (controller == null) {
            controller = MockSCMController.lookup(controllerId);
        }
        return controller;
    }

    public String getRepository() {
        return repository;
    }

    public String getHead() {
        if (head instanceof MockChangeRequestSCMHead) {
            return "CR-" + ((MockChangeRequestSCMHead) head).getNumber();
        }
        if (head instanceof MockTagSCMHead) {
            return "TAG:" + head.getName();
        }
        return head.getName();
    }

    public String getRevision() {
        if (revision instanceof MockSCMRevision) {
            return ((MockSCMRevision) revision).getHash();
        }
        if (revision instanceof MockChangeRequestSCMRevision) {
            return ((MockChangeRequestSCMRevision) revision).getHash();
        }
        return null;
    }

    @Override
    public boolean supportsPolling() {
        return true;
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Override
    public PollingResult compareRemoteRevisionWith(@Nonnull Job<?, ?> project, @Nullable Launcher launcher,
                                                   @Nullable FilePath workspace, @Nonnull TaskListener listener,
                                                   @Nonnull SCMRevisionState baseline)
            throws IOException, InterruptedException {
        if (baseline instanceof MockSCMRevisionState) {
            String revision;
            if (this.revision instanceof MockSCMRevision) {
                revision = ((MockSCMRevision) this.revision).getHash();
            } else if (this.revision instanceof MockChangeRequestSCMRevision) {
                revision = ((MockChangeRequestSCMRevision) this.revision).getHash();
            } else {
                if (head instanceof MockChangeRequestSCMHead) {
                    revision = controller()
                            .getRevision(repository, "change-request/" + ((MockChangeRequestSCMHead) head).getNumber());
                } else {
                    revision = controller().getRevision(repository, head.getName());
                }
            }
            if (((MockSCMRevisionState) baseline).getRevision().getHash().equals(revision)) {
                return PollingResult.NO_CHANGES;
            }
            return PollingResult.SIGNIFICANT;
        }
        return PollingResult.BUILD_NOW;
    }

    @Override
    public void checkout(@Nonnull Run<?, ?> build, @Nonnull Launcher launcher, @Nonnull FilePath workspace,
                         @Nonnull TaskListener listener, @CheckForNull File changelogFile,
                         @CheckForNull SCMRevisionState baseline) throws IOException, InterruptedException {
        String identifier;
        if (this.revision instanceof MockSCMRevision) {
            identifier = ((MockSCMRevision) this.revision).getHash();
        } else if (this.revision instanceof MockChangeRequestSCMRevision) {
            identifier = ((MockChangeRequestSCMRevision) this.revision).getHash();
        } else {
            identifier = head.getName();
        }
        String hash = controller().checkout(workspace, repository, identifier);
        FileWriter writer = new FileWriter(changelogFile);
        try {
            Items.XSTREAM2.toXML(controller().log(repository, hash), writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        build.addAction(new MockSCMRevisionState(new MockSCMRevision(head, hash)));
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(@Nonnull Run<?, ?> build, @Nullable FilePath workspace,
                                                   @Nullable Launcher launcher, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        return build.getAction(MockSCMRevisionState.class);
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new ChangeLogParser() {
            @Override
            public ChangeLogSet<? extends ChangeLogSet.Entry> parse(Run build, RepositoryBrowser<?> browser,
                                                                    File changelogFile)
                    throws IOException, SAXException {
                List<MockSCMController.LogEntry> entries =
                        (List<MockSCMController.LogEntry>) Items.XSTREAM2.fromXML(changelogFile);
                return new MockSCMChangeLogSet(build, browser, entries);
            }
        };
    }

    @Override
    public String toString() {
        return "MockSCM{" +
                "controllerId='" + controllerId + '\'' +
                ", repository='" + repository + '\'' +
                ", head=" + head +
                ", revision=" + revision +
                '}';
    }

    public static class MockSCMRevisionState extends SCMRevisionState {
        private final MockSCMRevision revision;

        public MockSCMRevisionState(MockSCMRevision revision) {
            this.revision = revision;
        }

        public MockSCMRevision getRevision() {
            return revision;
        }
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<MockSCM> {

        public DescriptorImpl() {
            super(MockSCMRepositoryBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "Mock SCM";
        }

        public ListBoxModel doFillControllerIdItems() {
            ListBoxModel result = new ListBoxModel();
            for (MockSCMController c : MockSCMController.all()) {
                result.add(c.getId());
            }
            return result;
        }

        public ListBoxModel doFillRepositoryItems(@QueryParameter String controllerId) throws IOException {
            ListBoxModel result = new ListBoxModel();
            MockSCMController c = MockSCMController.lookup(controllerId);
            if (c != null) {
                for (String r : c.listRepositories()) {
                    result.add(r);
                }
            }
            return result;
        }

        public ListBoxModel doFillHeadItems(@QueryParameter String controllerId, @QueryParameter String repository)
                throws IOException {
            ListBoxModel result = new ListBoxModel();
            MockSCMController c = MockSCMController.lookup(controllerId);
            if (c != null) {
                for (String r : c.listBranches(repository)) {
                    result.add(r);
                }
                for (String r : c.listTags(repository)) {
                    result.add("TAG:" + r);
                }
                for (Integer r : c.listChangeRequests(repository)) {
                    result.add("CR-" + r);
                }
            }
            return result;
        }
    }
}
