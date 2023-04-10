/*
 * The MIT License
 *
 * Copyright (c) 2015-2016 CloudBees, Inc.
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
 */

package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.Items;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import hudson.Extension;
import java.io.IOException;
import java.util.List;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Degenerate navigator which only ever returns a single repository.
 */
@Restricted(NoExternalUse.class)
public class SingleSCMNavigator extends SCMNavigator {

    private final String name;
    private final List<SCMSource> sources;

    @DataBoundConstructor
    public SingleSCMNavigator(String name, List<SCMSource> sources) {
        this.name = name;
        this.sources = sources;
    }

    public String getName() {
        return name;
    }

    public List<SCMSource> getSources() {
        return sources;
    }

    @NonNull
    @Override
    protected String id() {
        return Util.getDigestOf(Items.XSTREAM.toXML(sources)
                .replaceAll(" plugin=(('[^']+@[^']+')|(\"[^\"]+@[^\"]+\"))", "")) + "::" + name;
    }

    @Override
    public void visitSources(@NonNull SCMSourceObserver observer) throws IOException, InterruptedException {
        SCMSourceObserver.ProjectObserver projectObserver = observer.observe(name);
        for (SCMSource source : sources) {
            projectObserver.addSource(source);
        }
        projectObserver.complete();
    }

    @Symbol("fromSource")
    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.SingleSCMNavigator_DisplayName();
        }

        @Override
        public SCMNavigator newInstance(String name) {
            return null;
        }

    }

}
