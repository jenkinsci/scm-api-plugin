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
import java.io.Serializable;
import java.net.URL;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;

/**
 * Class retained for on-disk compatibility only for instances upgrading and deserializing data from disk.
 * @since 1.1
 * @deprecated use {@link ContributorMetadataAction} for the author information, {@link ObjectMetadataAction} for the
 * title and links and {@link ChangeRequestSCMHead#getId()} and
 * {@link ChangeRequestSCMHead#getTarget()}
 */
@Restricted(DoNotUse.class)
@Deprecated
public abstract class ChangeRequestAction extends InvisibleAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @CheckForNull
    public String getId() {
        return null;
    }

    @CheckForNull
    public URL getURL() {
        return null;
    }

    @CheckForNull
    public String getTitle() {
        return null;
    }

    @CheckForNull
    public String getAuthor() {
        return null;
    }

    @CheckForNull
    public String getAuthorDisplayName() {
        return null;
    }

    @CheckForNull
    public String getAuthorEmail() {
        return null;
    }

    @CheckForNull
    public SCMHead getTarget() {
        return null;
    }

}
