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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMProbeStat;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class MockSCMSource extends SCMSource {
    private final String controllerId;
    private final String repository;
    private final List<SCMSourceTrait> traits;
    private transient MockSCMController controller;

    @DataBoundConstructor
    public MockSCMSource(String controllerId, String repository, List<SCMSourceTrait> traits) {
        this.controllerId = controllerId;
        this.repository = repository;
        this.traits = SCMTrait.asSetList(traits);
    }

    public MockSCMSource(String controllerId, String repository, SCMSourceTrait... traits) {
        this(controllerId, repository, Arrays.asList(traits));
    }

    public MockSCMSource(MockSCMController controller, String repository, List<SCMSourceTrait> traits) {
        this.controllerId = controller.getId();
        this.controller = controller;
        this.repository = repository;
        this.traits = SCMTrait.asSetList(traits);
    }

    public MockSCMSource(MockSCMController controller, String repository, SCMSourceTrait... traits) {
        this(controller, repository, Arrays.asList(traits));
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

    public List<SCMSourceTrait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public String getRepository() {
        return repository;
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer,
                            @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener)
            throws IOException, InterruptedException {

        MockSCMSourceRequest request = new MockSCMSourceContext(this, criteria, observer)
                .withTraits(traits)
                .newRequest(this, listener);
        try {
            controller().applyLatency();
            controller().checkFaults(repository, null, null, false);
            if (request.isFetchBranches()) {
                for (final String branch : controller().listBranches(repository)) {
                    if (request.process(new MockSCMHead(branch),
                            new SCMSourceRequest.RevisionLambda<MockSCMHead, MockSCMRevision>() {
                                @NonNull
                                @Override
                                public MockSCMRevision create(@NonNull MockSCMHead head)
                                        throws IOException, InterruptedException {
                                    controller().applyLatency();
                                    controller().checkFaults(repository, head.getName(), null, false);
                                    return new MockSCMRevision(head, controller().getRevision(repository, branch));
                                }
                            }, new SCMSourceRequest.ProbeLambda<MockSCMHead, MockSCMRevision>() {
                                @NonNull
                                @Override
                                public SCMSourceCriteria.Probe create(@NonNull MockSCMHead head, @Nullable
                                        MockSCMRevision revisionInfo) throws IOException, InterruptedException {
                                    controller().applyLatency();
                                    controller().checkFaults(repository, head.getName(), revisionInfo.getHash(), false);
                                    return new MockSCMProbe(head, revisionInfo.getHash());
                                }
                            })) {
                        return;
                    }
                }
            }
            if (request.isFetchTags()) {
                for (final String tag : controller().listTags(repository)) {
                    if (request.process(new MockTagSCMHead(tag, controller().getTagTimestamp(repository, tag)),
                            new SCMSourceRequest.RevisionLambda<MockTagSCMHead, MockSCMRevision>() {
                                @NonNull
                                @Override
                                public MockSCMRevision create(@NonNull MockTagSCMHead head)
                                        throws IOException, InterruptedException {
                                    controller().applyLatency();
                                    controller().checkFaults(repository, head.getName(), null, false);
                                    return new MockSCMRevision(head, controller().getRevision(repository, tag));
                                }
                            }, new SCMSourceRequest.ProbeLambda<MockTagSCMHead, MockSCMRevision>() {
                                @NonNull
                                @Override
                                public SCMSourceCriteria.Probe create(@NonNull MockTagSCMHead head, @Nullable
                                        MockSCMRevision revisionInfo)
                                        throws IOException, InterruptedException {
                                    controller().applyLatency();
                                    controller().checkFaults(repository, head.getName(), revisionInfo.getHash(), false);
                                    return new MockSCMProbe(head, revisionInfo.getHash());
                                }
                            })) {
                        return;
                    }
                }
            }
            if (request.isFetchChangeRequests()) {
                for (final Integer number : controller().listChangeRequests(repository)) {
                    checkInterrupt();
                    String target = controller().getTarget(repository, number);
                    Set<MockChangeRequestFlags> crFlags = controller.getFlags(repository, number);
                    Set<ChangeRequestCheckoutStrategy> strategies = request.getCheckoutStrategies();
                    boolean singleStrategy = strategies.size() == 1;
                    for (ChangeRequestCheckoutStrategy strategy : strategies) {
                        if (request.process(new MockChangeRequestSCMHead(
                                        crFlags.contains(MockChangeRequestFlags.FORK)
                                                ? new SCMHeadOrigin.Fork("fork")
                                                : null,
                                        number, target, strategy, singleStrategy),
                                new SCMSourceRequest.RevisionLambda<MockChangeRequestSCMHead,
                                        MockChangeRequestSCMRevision>() {
                                    @NonNull
                                    @Override
                                    public MockChangeRequestSCMRevision create(@NonNull MockChangeRequestSCMHead head)
                                            throws IOException, InterruptedException {
                                        controller().applyLatency();
                                        controller().checkFaults(repository, head.getName(), null, false);
                                        String revision =
                                                controller().getRevision(repository, "change-request/" + number);
                                        String targetRevision =
                                                controller().getRevision(repository, head.getTarget().getName());
                                        return new MockChangeRequestSCMRevision(head,
                                                new MockSCMRevision(head.getTarget(), targetRevision), revision);
                                    }
                                },
                                new SCMSourceRequest.ProbeLambda<MockChangeRequestSCMHead,
                                        MockChangeRequestSCMRevision>() {
                                    @NonNull
                                    @Override
                                    public SCMSourceCriteria.Probe create(@NonNull MockChangeRequestSCMHead head,
                                                                          @Nullable
                                                                                  MockChangeRequestSCMRevision revisionInfo)
                                            throws IOException, InterruptedException {
                                        controller().applyLatency();
                                        controller()
                                                .checkFaults(repository, head.getName(), revisionInfo.getHash(), false);
                                        return new MockSCMProbe(head, revisionInfo.getHash());
                                    }
                                })) {
                            return;
                        }
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(request);
        }
    }

    @NonNull
    @Override
    public SCM build(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        return new MockSCMBuilder(this, head, revision)
                .withTraits(traits)
                .build();
    }


    @NonNull
    @Override
    protected List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        controller().applyLatency();
        controller().checkFaults(repository, null, null, true);
        List<Action> result = new ArrayList<Action>();
        result.add(new MockSCMLink("source"));
        String description = controller().getDescription(repository);
        String displayName = controller().getDisplayName(repository);
        String url = controller().getUrl(repository);
        String iconClassName = controller().getRepoIconClassName();
        if (description != null || displayName != null || url != null) {
            result.add(new ObjectMetadataAction(displayName, description, url));
        }
        if (iconClassName != null) {
            result.add(new MockAvatarMetadataAction(iconClassName));
        }
        return result;
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMRevision revision,
                                           @CheckForNull SCMHeadEvent event,
                                           @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        controller().applyLatency();
        String hash;
        if (revision instanceof MockSCMRevision) {
            hash = ((MockSCMRevision) revision).getHash();
        } else if (revision instanceof MockChangeRequestSCMRevision) {
            hash = ((MockChangeRequestSCMRevision) revision).getHash();
        } else {
            throw new IOException("Unexpected revision");
        }
        controller().checkFaults(repository, revision.getHead().getName(), hash, true);
        return Collections.<Action>singletonList(new MockSCMLink("revision"));
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMHead head,
                                           @CheckForNull SCMHeadEvent event,
                                           @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        controller().applyLatency();
        controller().checkFaults(repository, head.getName(), null, true);
        List<Action> result = new ArrayList<Action>();
        if (head instanceof MockChangeRequestSCMHead) {
            result.add(new ContributorMetadataAction(
                    "bob",
                    "Bob Smith",
                    "bob@example.com"
            ));
            result.add(new ObjectMetadataAction(
                    String.format("Change request #%d", ((MockChangeRequestSCMHead) head).getNumber()),
                    null,
                    "http://changes.example.com/" + ((MockChangeRequestSCMHead) head).getId()
            ));
        }
        result.add(new MockSCMLink("branch"));
        return result;
    }

    @Override
    protected boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        for (SCMSourceTrait trait: traits) {
            if (trait.isCategoryEnabled(category)) {
                return true;
            }
        }
        return false;
    }

    @Symbol("mockScm")
    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {
        @Nonnull
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

        @NonNull
        @Override
        protected SCMHeadCategory[] createCategories() {
            return new SCMHeadCategory[]{
                    UncategorizedSCMHeadCategory.DEFAULT,
                    ChangeRequestSCMHeadCategory.DEFAULT,
                    TagSCMHeadCategory.DEFAULT
            };
        }

        public List<SCMSourceTraitDescriptor> getTraitsDescriptors() {
            return SCMSourceTrait._for(this, MockSCMSourceContext.class, MockSCMBuilder.class);
        }

        public List<SCMSourceTrait> getTraitsDefaults() {
            return Collections.<SCMSourceTrait>singletonList(new MockSCMDiscoverBranches());
        }
    }

    private class MockSCMProbe extends SCMProbe {
        private final String revision;
        private final SCMHead head;

        public MockSCMProbe(SCMHead head, String revision) {
            this.revision = revision;
            this.head = head;
        }

        @NonNull
        @Override
        public SCMProbeStat stat(@NonNull String path) throws IOException {
            return SCMProbeStat.fromType(controller().stat(repository, revision, path));
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public String name() {
            return head.getName();
        }

        @Override
        public long lastModified() {
            return controller().lastModified(repository, revision);
        }
    }
}
