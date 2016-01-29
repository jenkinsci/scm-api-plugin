/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.model.TopLevelItemDescriptor;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A single fixed branch using a legacy SCM implementation.
 *
 * @author Stephen Connolly
 */
public class SingleSCMSource extends SCMSource {

    /**
     * The name of the branch.
     */
    private final String name;

    /**
     * The SCM configuration.
     */
    private final SCM scm;

    /**
     * Lazy instance singleton-ish.
     */
    private transient SCMHead head;
    private transient SCMRevisionImpl revisionHash;

    /**
     * Our constructor.
     *
     * @param id   source id.
     * @param name the name of the branch.
     * @param scm  the configuration.
     */
    @SuppressWarnings("unused") // stapler
    @DataBoundConstructor
    public SingleSCMSource(String id, String name, SCM scm) {
        super(id);
        this.name = name;
        this.scm = scm;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected synchronized void retrieve(@NonNull SCMHeadObserver observer,
                                         @NonNull TaskListener listener)
            throws IOException {
        if (head == null) {
            head = new SCMHead(name);
            revisionHash = new SCMRevisionImpl(head);
        }
        observer.observe(head, revisionHash);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SCM build(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        if (name.equals(head.getName())) {
            return scm;
        }
        return new NullSCM();
    }

    /**
     * Our revision class.
     */
    private static class SCMRevisionImpl extends SCMRevision {
        /**
         * Constructor.
         *
         * @param head the head.
         */
        public SCMRevisionImpl(SCMHead head) {
            super(head);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDeterministic() {
            return false; // we don't know what the underlying SCM supports, so it is not deterministic.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SCMRevisionImpl that = (SCMRevisionImpl) o;

            return getHead().equals(that.getHead());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return getHead().hashCode();
        }
    }

    /**
     * Our descriptor.
     */
    @Extension
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends SCMSourceDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.SingleSCMSource_DisplayName();
        }

        /**
         * Returns the {@link SCMDescriptor} instances that are appropriate for the current context.
         *
         * @param context the current context.
         * @return the {@link SCMDescriptor} instances
         */
        @SuppressWarnings("unused") // used by stapler binding
        public static List<SCMDescriptor<?>> getSCMDescriptors(@AncestorInPath SCMSourceOwner context) {
            List<SCMDescriptor<?>> result = new ArrayList<SCMDescriptor<?>>(SCM.all());
            for (Iterator<SCMDescriptor<?>> iterator = result.iterator(); iterator.hasNext(); ) {
                SCMDescriptor<?> d = iterator.next();
                if (NullSCM.class.equals(d.clazz)) {
                    iterator.remove();
                }
            }
            if (context != null && context instanceof Describable) {
                final Descriptor descriptor = ((Describable) context).getDescriptor();
                if (descriptor instanceof TopLevelItemDescriptor) {
                    final TopLevelItemDescriptor topLevelItemDescriptor = (TopLevelItemDescriptor) descriptor;
                    for (Iterator<SCMDescriptor<?>> iterator = result.iterator(); iterator.hasNext(); ) {
                        SCMDescriptor<?> d = iterator.next();
                        if (!topLevelItemDescriptor.isApplicable(d)) {
                            iterator.remove();
                        }
                    }
                }
            }
            return result;
        }
    }
}
