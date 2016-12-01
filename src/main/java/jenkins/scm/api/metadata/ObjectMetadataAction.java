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
 */

package jenkins.scm.api.metadata;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.InvisibleAction;
import java.io.Serializable;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Holds metadata about the object referenced by a {@link SCMRevision}, {@link SCMHead}, {@link SCMSource} or {@link SCMNavigator}.
 *
 * @since 2.0
 */
@ExportedBean
public class ObjectMetadataAction extends InvisibleAction implements Serializable {
    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    @CheckForNull
    private final String objectDisplayName;
    @CheckForNull
    private final String objectDescription;
    @CheckForNull
    private final String objectUrl;

    public ObjectMetadataAction(@CheckForNull String objectDisplayName,
                                @CheckForNull String objectDescription,
                                @CheckForNull String objectUrl) {
        this.objectDisplayName = objectDisplayName;
        this.objectDescription = objectDescription;
        this.objectUrl = objectUrl;
    }

    /**
     * Returns the display name of the object or {@code null}.
     *
     * @return the display name of the object or {@code null}
     */
    @Exported
    @CheckForNull
    // note we need to call this getObjectDisplayName() to avoid conflicting with InvisibleAction.getDisplayName()
    public String getObjectDisplayName() {
        return objectDisplayName;
    }

    /**
     * Returns the description of the object or {@code null}.
     *
     * @return the description of the object or {@code null}
     */
    @Exported
    @CheckForNull
    public String getObjectDescription() {
        return objectDescription;
    }

    /**
     * Returns the external url of the object or {@code null} if the object does not have an external url.
     *
     * @return the display name of the object or {@code null}
     */
    @Exported
    @CheckForNull
    // note we need to call this getObjectDisplayName() to avoid conflicting with InvisibleAction.getUrl()
    public String getObjectUrl() {
        return objectUrl;
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

        ObjectMetadataAction that = (ObjectMetadataAction) o;

        if (objectDisplayName != null
                ? !objectDisplayName.equals(that.objectDisplayName)
                : that.objectDisplayName != null) {
            return false;
        }
        if (objectDescription != null
                ? !objectDescription.equals(that.objectDescription)
                : that.objectDescription != null) {
            return false;
        }
        return objectUrl != null ? objectUrl.equals(that.objectUrl) : that.objectUrl == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = objectDisplayName != null ? objectDisplayName.hashCode() : 0;
        result = 31 * result + (objectDescription != null ? objectDescription.hashCode() : 0);
        result = 31 * result + (objectUrl != null ? objectUrl.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ObjectMetadataAction{" +
                "objectDisplayName='" + objectDisplayName + '\'' +
                ", objectDescription='" + objectDescription + '\'' +
                ", objectUrl='" + objectUrl + '\'' +
                '}';
    }
}
