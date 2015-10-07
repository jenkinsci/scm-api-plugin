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
package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A {@link Descriptor} for {@link SCMSource}s.
 *
 * @author Stephen Connolly
 */
public abstract class SCMSourceDescriptor extends Descriptor<SCMSource> {

    /**
     * Return or generate the ID for a source instance.
     *
     * @param source the source or {@code null} if a new source.
     * @return the ID of the supplied source or a newly generated ID to use for a new source instance.
     */
    @NonNull
    @SuppressWarnings("unused") // use by stapler as well as elsewhere
    public String getId(@CheckForNull SCMSource source) {
        return source == null ? UUID.randomUUID().toString() : source.getId();
    }

    /**
     * Returns {@code true} if this source type is applicable to the given owner.
     *
     * @param owner the type of owner.
     * @return true to allow user to select and configure this source.
     */
    public boolean isApplicable(java.lang.Class<? extends SCMSourceOwner> owner) {
        return true;
    }

    /**
     * Returns {@code true} if the source type is one that the user is permitted to configure. Where a source is
     * used to wrap or decorate another source it may make sense to return {@code false}.
     *
     * @return {@code true} if the source type is one that the user is permitted to configure via the UI.
     */
    @Restricted(NoExternalUse.class)
    public boolean isUserInstantiable() {
        return true;
    }

    /**
     * Returns the list of descriptors that are appropriate for a specified owner and {@link #isUserInstantiable()}.
     *
     * @param owner the owner.
     * @return the list of descriptors
     */
    @NonNull
    @SuppressWarnings("unused") // used by stapler
    public static List<SCMSourceDescriptor> forOwner(@CheckForNull SCMSourceOwner owner) {
        return forOwner(owner == null ? SCMSourceOwner.class : owner.getClass(), true);
    }

    /**
     * Returns the list of descriptors that are appropriate for a specified owner with the additional filter by
     * {@link #isUserInstantiable()}.
     *
     * @param owner             the owner.
     * @param onlyUserInstantiable {@code true} if only those descriptors that are {@link #isUserInstantiable()}.
     * @return the list of descriptors
     */
    @NonNull
    @SuppressWarnings("unused") // used by stapler
    public static List<SCMSourceDescriptor> forOwner(@CheckForNull SCMSourceOwner owner,
                                                     boolean onlyUserInstantiable) {
        return forOwner(owner == null ? SCMSourceOwner.class : owner.getClass(), onlyUserInstantiable);
    }

    /**
     * Returns the list of descriptors that are appropriate for a specified type of owner and
     * {@link #isUserInstantiable()}.
     *
     * @param clazz the type of owner.
     * @return the list of descriptors
     */
    @NonNull
    @SuppressWarnings("unused") // used by stapler
    public static List<SCMSourceDescriptor> forOwner(Class<? extends SCMSourceOwner> clazz) {
        return forOwner(clazz, true);
    }

    /**
     * Returns the list of descriptors that are appropriate for a specified type of owner with the additional
     * filter by
     * {@link #isUserInstantiable()}.
     *
     * @param clazz                the type of owner.
     * @param onlyUserInstantiable {@code true} if only those descriptors that are {@link #isUserInstantiable()}.
     * @return the list of descriptors
     */
    @NonNull
    @SuppressWarnings("unused") // used by stapler
    public static List<SCMSourceDescriptor> forOwner(Class<? extends SCMSourceOwner> clazz,
                                                     boolean onlyUserInstantiable) {
        List<SCMSourceDescriptor> result = new ArrayList<SCMSourceDescriptor>();
        Jenkins j = Jenkins.getInstance();
        if (j == null) {
            return result;
        }
        for (Descriptor<SCMSource> d : j.getDescriptorList(SCMSource.class)) { // TODO 1.572+ ExtensionList.lookup
            if (d instanceof SCMSourceDescriptor) {
                SCMSourceDescriptor descriptor = (SCMSourceDescriptor) d;
                if (descriptor.isApplicable(clazz) && (!onlyUserInstantiable || descriptor.isUserInstantiable())) {
                    result.add(descriptor);
                }
            }
        }
        return result;
    }

}
