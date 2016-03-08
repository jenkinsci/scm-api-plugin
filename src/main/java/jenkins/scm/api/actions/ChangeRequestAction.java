/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
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

package jenkins.scm.api.actions;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.InvisibleAction;
import hudson.model.TaskListener;
import java.net.URL;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Indicates that an {@link SCMHead} represents a registered proposed change, such as a pull request.
 * <p>Intended to capture concepts common to popular code review systems and which might warrant generic UI representation.
 * Fields may be null in case the corresponding concept does not exist in the system being represented.
 * <p>Should be restricted to those aspects of a change which are either immutable
 * or otherwise not affected by changes to the head {@link SCMRevision}
 * (as opposed to, say, mergeability status).
 * Should also be restricted to short metadata which can be quickly retrieved during {@link SCMSource#retrieve(SCMHeadObserver, TaskListener)}.
 * @see SCMHead#getAllActions
 * @since FIXME
 */
@ExportedBean
public abstract class ChangeRequestAction extends InvisibleAction {

    /**
     * Identifier of this change request.
     * Expected to be unique among requests coming from a given {@link SCMSource}.
     * @return an ID of some kind, such as a pull request number (decimal) or a Gerrit change ID
     */
    @Exported
    @CheckForNull
    public String getId() {
        return null;
    }

    /**
     * Link to web representation of change.
     * @return an HTTP(S) permalink
     */
    @Exported
    @CheckForNull
    public URL getURL() {
        return null;
    }

    /**
     * Display title.
     * @return a summary message
     */
    @Exported
    @CheckForNull
    public String getTitle() {
        return null;
    }

    /**
     * Username of author of the proposed change.
     * @return a user login name or other unique user identifier
     */
    @Exported
    @CheckForNull
    public String getAuthor() {
        return null;
    }

    /**
     * Human name of author of proposed change.
     * @return First M. Last, etc.
     */
    @Exported
    @CheckForNull
    public String getAuthorDisplayName() {
        return null;
    }

    /**
     * Email address of author of proposed change.
     * @return a valid email address
     */
    @Exported
    @CheckForNull
    public String getAuthorEmail() {
        return null;
    }

    /**
     * Branch to which this change would be merged or applied if it were accepted.
     * @return a “target” or “base” branch
     */
    @Exported
    @CheckForNull
    public SCMHead getTarget() {
        return null;
    }

}
