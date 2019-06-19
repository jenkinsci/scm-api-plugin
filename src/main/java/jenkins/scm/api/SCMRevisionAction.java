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
import hudson.RestrictedSince;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * {@link Action} added to {@link AbstractBuild} to remember
 * which revision is built in the given build.
 */
@ExportedBean
public class SCMRevisionAction extends InvisibleAction {
    /**
     * The {@link SCMSource#getId()} or {@code null} for legacy instances.
     *
     * @since 2.2.0
     */
    @CheckForNull
    private final String sourceId;
    /**
     * The {@link SCMRevision}.
     */
    @NonNull
    private final SCMRevision revision;

    /**
     * Constructor.
     *
     * @param revision the {@link SCMRevision}.
     * @deprecated use {@link #SCMRevisionAction(SCMSource, SCMRevision)}
     */
    @Restricted(DoNotUse.class)
    @RestrictedSince("2.2.0")
    @Deprecated
    public SCMRevisionAction(@NonNull SCMRevision revision) {
        this(revision, null);
    }

    /**
     * Constructor.
     *
     * @param source   the {@link SCMSource}.
     * @param revision the {@link SCMRevision}.
     */
    public SCMRevisionAction(@NonNull SCMSource source, @NonNull SCMRevision revision) {
        this(revision, source.getId());
    }

    /**
     * Common constructor (exposed for tests)
     *
     * @param revision the revision.
     * @param sourceId the source id.
     */
    /*package*/ SCMRevisionAction(@NonNull SCMRevision revision, @CheckForNull String sourceId) {
        revision.getClass(); // fail fast if null
        this.sourceId = sourceId;
        this.revision = revision;
    }

    /**
     * Gets the {@link SCMRevision}.
     *
     * @return the {@link SCMRevision}.
     */
    @Exported
    @NonNull
    public SCMRevision getRevision() {
        return revision;
    }

    /**
     * Gets the {@link SCMSource#getId()} that the revision was created for.
     *
     * @return the {@link SCMSource#getId()} that the revision was created for or {@code null} when legacy data
     * @since 2.2.0
     */
    @CheckForNull
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Gets the {@link SCMRevision} from the specified {@link Actionable}.
     *
     * @param actionable {@link Actionable} containing a possible {@link SCMRevisionAction}.
     * @return the {@link SCMRevision}.
     * @deprecated use {@link #getRevision(SCMSource, Actionable)}
     */
    @Deprecated
    @Restricted(DoNotUse.class)
    @RestrictedSince("2.2.0")
    @CheckForNull
    public static SCMRevision getRevision(@NonNull Actionable actionable) {
        SCMRevisionAction action = actionable.getAction(SCMRevisionAction.class);
        return action != null ? action.getRevision() : null;
    }

    /**
     * Gets the {@link SCMRevision} for the specified {@link SCMSource} from the specified {@link Actionable}.
     *
     * @param source     the {@link SCMSource} to get the revision for.
     * @param actionable {@link Actionable} containing a possible {@link SCMRevisionAction}.
     * @return the {@link SCMRevision}.
     */
    @CheckForNull
    public static SCMRevision getRevision(@NonNull SCMSource source, @NonNull Actionable actionable) {
        String sourceId = source.getId();
        SCMRevisionAction fallback = null;
        for (SCMRevisionAction a : actionable.getActions(SCMRevisionAction.class)) {
            if (sourceId.equals(a.getSourceId())) {
                return a.getRevision();
            }
            if (a.getSourceId() == null && fallback == null) {
                // preserve legacy behaviour of first match when dealing with legacy data
                fallback = a;
            }
        }
        return fallback == null ? null : fallback.getRevision();
    }
}
