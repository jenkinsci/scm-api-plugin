/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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
import java.util.List;
import javax.annotation.Nonnull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import org.kohsuke.stapler.DataBoundConstructor;

public class MockSCMNavigator extends SCMNavigator {

    private final String controllerId;
    private final boolean includeBranches;
    private final boolean includeTags;
    private final boolean includeChangeRequests;
    private transient MockSCMController controller;

    @DataBoundConstructor
    public MockSCMNavigator(String controllerId, boolean includeBranches, boolean includeTags,
                            boolean includeChangeRequests) {
        this.controllerId = controllerId;
        this.includeBranches = includeBranches;
        this.includeTags = includeTags;
        this.includeChangeRequests = includeChangeRequests;
    }

    public MockSCMNavigator(MockSCMController controller, boolean includeBranches, boolean includeTags,
                            boolean includeChangeRequests) {
        this.controllerId = controller.getId();
        this.controller = controller;
        this.includeBranches = includeBranches;
        this.includeTags = includeTags;
        this.includeChangeRequests = includeChangeRequests;
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

    public boolean isIncludeBranches() {
        return includeBranches;
    }

    public boolean isIncludeTags() {
        return includeTags;
    }

    public boolean isIncludeChangeRequests() {
        return includeChangeRequests;
    }

    @Override
    protected String id() {
        return controllerId;
    }

    @Override
    public void visitSources(@NonNull SCMSourceObserver observer) throws IOException, InterruptedException {
        for (String name : controller().listRepositories()) {
            if (!observer.isObserving()) {
                return;
            }
            checkInterrupt();
            SCMSourceObserver.ProjectObserver po = observer.observe(name);
            po.addSource(new MockSCMSource(getId() + "::" + name,
                    controller, name, includeBranches, includeTags, includeChangeRequests));
            po.complete();
        }
    }

    @NonNull
    @Override
    public List<Action> retrieveActions(@NonNull SCMNavigatorOwner owner,
                                        @CheckForNull SCMNavigatorEvent event,
                                        @NonNull TaskListener listener)
            throws IOException, InterruptedException {
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
    }
}
