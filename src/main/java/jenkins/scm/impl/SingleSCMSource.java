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
import hudson.RestrictedSince;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.TopLevelItemDescriptor;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

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
     * @param name the name of the branch.
     * @param scm  the configuration.
     * @since 2.2.0
     */
    @SuppressWarnings("unused") // stapler
    @DataBoundConstructor
    public SingleSCMSource(String name, SCM scm) {
        this.name = name;
        this.scm = scm;
    }

    /**
     * Legacy constructor.
     *
     * @param id   source id.
     * @param name the name of the branch.
     * @param scm  the configuration.
     * @deprecated use {@link #SingleSCMSource(String, SCM)} and {@link #setId(String)}.
     */
    @Restricted(NoExternalUse.class)
    @RestrictedSince("2.2.0")
    @Deprecated
    public SingleSCMSource(String id, String name, SCM scm) {
        setId(id);
        this.name = name;
        this.scm = scm;
    }

    public String getName() {
        return name;
    }
    
    public SCM getScm() {
        return scm;
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer,
                            @CheckForNull SCMHeadEvent<?> event, @CheckForNull Item context,
                            @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        if (head == null) {
            head = new SCMHead(name);
            revisionHash = new SCMRevisionImpl(head);
        }
        // we ignore the criteria as this was an explicitly called out SCM and thus it always matches
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SingleSCMSource{" +
                "super=" + super.toString() +
                ", name='" + name + '\'' +
                ", scm=" + scm +
                ", head=" + head +
                ", revisionHash=" + revisionHash +
                '}';
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
    @Symbol("fromScm")
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
            if (context instanceof Describable) {
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
