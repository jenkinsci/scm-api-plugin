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
import java.io.Serializable;
import jenkins.scm.api.ChangeRequestSCMHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Holds metadata about the contributor of a {@link SCMRevision}, {@link SCMHead}, {@link SCMSource} or
 * {@link SCMNavigator}.
 * <p>
 * In general we expect this to be reported from {@link SCMSource#fetchActions(SCMHead, SCMHeadEvent, TaskListener)}
 * for {@link ChangeRequestSCMHead} instances but can be used elsewhere where that makes sense.
 *
 * @since 2.0
 */
@ExportedBean
public class ContributorMetadataAction extends InvisibleAction implements Serializable {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Username of contributor.
     */
    @CheckForNull
    private final String contributor;
    /**
     * Human name of contributor.
     */
    @CheckForNull
    private final String contributorDisplayName;
    /**
     * Email address of contributor.
     */
    @CheckForNull
    private final String contributorEmail;

    public ContributorMetadataAction(String contributor, String contributorDisplayName, String contributorEmail) {
        this.contributor = contributor;
        this.contributorDisplayName = contributorDisplayName;
        this.contributorEmail = contributorEmail;
    }

    /**
     * Username of author of the proposed change.
     * @return a user login name or other unique user identifier
     */
    @Exported
    @CheckForNull
    public String getContributor() {
        return contributor;
    }

    /**
     * Human name of author of proposed change.
     * @return First M. Last, etc.
     */
    @Exported
    @CheckForNull
    public String getContributorDisplayName() {
        return contributorDisplayName;
    }

    /**
     * Email address of author of proposed change.
     * @return a valid email address
     */
    @Exported
    @CheckForNull
    public String getContributorEmail() {
        return contributorEmail;
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

        ContributorMetadataAction that = (ContributorMetadataAction) o;

        if (contributor != null ? !contributor.equals(that.contributor) : that.contributor != null) {
            return false;
        }
        if (contributorDisplayName != null
                ? !contributorDisplayName.equals(that.contributorDisplayName)
                : that.contributorDisplayName != null) {
            return false;
        }
        return contributorEmail != null
                ? contributorEmail.equals(that.contributorEmail)
                : that.contributorEmail == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = contributor != null ? contributor.hashCode() : 0;
        result = 31 * result + (contributorDisplayName != null ? contributorDisplayName.hashCode() : 0);
        result = 31 * result + (contributorEmail != null ? contributorEmail.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ContributorMetadataAction{" +
                "contributor='" + contributor + '\'' +
                ", contributorDisplayName='" + contributorDisplayName + '\'' +
                ", contributorEmail='" + contributorEmail + '\'' +
                '}';
    }


}
