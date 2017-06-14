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
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.api.trait.SCMTraitDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MockSCMNavigator extends SCMNavigator {

    private final String controllerId;
    private final List<SCMTrait<?>> traits;
    private transient MockSCMController controller;

    @DataBoundConstructor
    public MockSCMNavigator(String controllerId, List<SCMTrait<?>> traits) {
        this.controllerId = controllerId;
        this.traits = new ArrayList<SCMTrait<?>>(traits);
    }

    public MockSCMNavigator(String controllerId, SCMTrait<?>... traits) {
        this(controllerId, Arrays.asList(traits));
    }

    public MockSCMNavigator(MockSCMController controller, List<SCMTrait<?>> traits) {
        this.controllerId = controller.getId();
        this.controller = controller;
        this.traits = new ArrayList<SCMTrait<?>>(traits);
    }

    public MockSCMNavigator(MockSCMController controller, SCMTrait<?>... traits) {
        this(controller, Arrays.asList(traits));
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

    public List<SCMTrait<?>> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    @Override
    protected String id() {
        return controllerId;
    }

    @Override
    public void visitSources(@NonNull SCMSourceObserver observer) throws IOException, InterruptedException {
        final MockSCMNavigatorRequest request = new MockSCMNavigatorContext()
                .withTraits(traits)
                .newRequest(this, observer);
        try {
            controller().applyLatency();
            controller().checkFaults(null, null, null, false);
            for (String name : controller().listRepositories()) {
                if (!request.isExcluded(name)) { // hack to allow the latency and faults to work
                    controller().applyLatency();
                    controller().checkFaults(name, null, null, false);
                    if (request.process(name, new SCMNavigatorRequest.SourceLambda() {
                        @NonNull
                        @Override
                        public SCMSource create(@NonNull String name) {
                            return new MockSCMSourceBuilder(getId() + "::" + name, controller, name)
                                    .withRequest(request)
                                    .build();
                        }
                    }, null)) {
                        return;
                    }
                }
            }
        } finally {
            request.close();
        }
    }

    @NonNull
    @Override
    public List<Action> retrieveActions(@NonNull SCMNavigatorOwner owner,
                                        @CheckForNull SCMNavigatorEvent event,
                                        @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        controller().applyLatency();
        controller().checkFaults(null, null, null, true);
        List<Action> result = new ArrayList<Action>();
        result.add(new MockSCMLink("organization"));
        String description = controller().getDescription();
        String displayName = controller().getDisplayName();
        String url = controller().getUrl();
        String iconClassName = controller().getOrgIconClassName();
        if (description != null || displayName != null || url != null) {
            result.add(new ObjectMetadataAction(displayName, description, url));
        }
        if (iconClassName != null) {
            result.add(new MockAvatarMetadataAction(iconClassName));
        }
        return result;
    }

    @Symbol("mockScm")
    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Mock SCM";
        }

        @Override
        public SCMNavigator newInstance(@CheckForNull String name) {
            return null;
        }

        public ListBoxModel doFillControllerIdItems() {
            ListBoxModel result = new ListBoxModel();
            for (MockSCMController c : MockSCMController.all()) {
                result.add(c.getId());
            }
            return result;
        }

        public List<SCMTraitDescriptor<?>> getTraitsDescriptors() {
            List<SCMTraitDescriptor<?>> descriptors = new ArrayList<SCMTraitDescriptor<?>>();
            descriptors.addAll(SCMNavigatorTrait._for(MockSCMNavigatorContext.class, MockSCMSourceBuilder.class));
            descriptors.addAll(SCMSourceTrait._for(MockSCMSourceContext.class, MockSCMBuilder.class));
            return descriptors;
        }

        public List<SCMTrait<?>> getTraitsDefaults() {
            return Collections.<SCMTrait<?>>singletonList(new MockSCMDiscoverBranches());
        }
    }
}
