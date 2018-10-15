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
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.InvisibleAction;
import java.io.Serializable;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.impl.avatars.AvatarCache;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Holds metadata about an avatar to be associated with a {@link SCMSource} or {@link SCMNavigator} (also valid for
 * {@link SCMRevision} and {@link SCMHead}, but would be considered unusual).
 * <p>
 * For example:
 * <ul>
 * <li>A {@link SCMNavigator} implementation that corresponds to a GitHub Team could use the
 * {@link #getAvatarImageOf(String)} to return the GitHub Team logo</li>
 * </ul>
 *
 * @since 2.0
 */
@ExportedBean
public abstract class AvatarMetadataAction extends InvisibleAction implements Serializable {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Returns the {@link Icon} class specification for the avatar of this object
     *
     * @return the {@link Icon} class specification or {@code null} to check {@link #getAvatarImageOf(String)}.
     * @see IconSpec#getIconClassName()
     * @throws IllegalStateException if called outside of a request handling thread.
     */
    @CheckForNull
    public String getAvatarIconClassName() {
        return null;
    }

    /**
     * Returns the description to use for the tool tip of the avatar of this object.
     *
     * @return the avatar description or {@code null}
     * @throws IllegalStateException if called outside of a request handling thread.
     */
    @CheckForNull
    public String getAvatarDescription() {
        return null;
    }

    /**
     * Returns the URL of the avatar of this object in the requested size or {@code null} if there is no avatar for
     * this object. Ideally only {@link #getAvatarIconClassName()} would be used, but to allow for the case where
     * the avatar is hosted on an external server this method can be used to retrieve the avatar image.
     *
     * @param size the size, the following sizes must be supported: {@code 16x16}, {@code 24x24}, {@code 32x32} and
     *             {@code 48x48}.
     * @return the url or {@code null}
     * @throws IllegalStateException if called outside of a request handling thread.
     */
    @CheckForNull
    public String getAvatarImageOf(@NonNull String size) {
        return avatarIconClassNameImageOf(getAvatarIconClassName(), size);
    }

    /**
     * Helper method to resolve the icon image url.
     *
     * @param iconClassName the icon class name.
     * @param size          the size string, e.g. {@code 16x16}, {@code 24x24}, etc.
     * @return the icon image url or {@code null}
     * @throws IllegalStateException if called outside of a request handling thread.
     */
    @CheckForNull
    protected final String avatarIconClassNameImageOf(@CheckForNull String iconClassName, @NonNull String size) {
        if (StringUtils.isNotBlank(iconClassName)) {
            String spec = null;
            if ("16x16".equals(size)) {
                spec = "icon-sm";
            } else if ("24x24".equals(size)) {
                spec = "icon-md";
            } else if ("32x32".equals(size)) {
                spec = "icon-lg";
            } else if ("48x48".equals(size)) {
                spec = "icon-xlg";
            }
            if (spec != null) {
                Icon icon = IconSet.icons.getIconByClassSpec(iconClassName + " " + spec);
                if (icon != null) {
                    JellyContext ctx = new JellyContext();
                    StaplerRequest currentRequest = Stapler.getCurrentRequest();
                    if (currentRequest == null) {
                        throw new IllegalStateException(
                                "cannot call avatarIconClassNameImageOf from outside a request handling thread"
                        );
                    }
                    ctx.setVariable("resURL", currentRequest.getContextPath() + Jenkins.RESOURCE_PATH);
                    return icon.getQualifiedUrl(ctx);
                }
            }
        }
        return null;
    }

    /**
     * Helper method to resolve an external image, cache it and resize it to the specified size. If the external URL
     * cannot be retrieved by Jenkins, then a randomly generated avatar will be generated using the external URL as
     * a seed (so that the generated avatar image for any given external URL will be consistent across both Jenkins
     * instances and restarts).
     *
     * @param url  the URL of the image to cache.
     * @param size the size string, e.g. {@code 16x16}, {@code 24x24}, etc.
     * @return the icon image url.
     * @throws IllegalStateException if called outside of a request handling thread.
     * @since 2.2.0
     */
    @NonNull
    protected final String cachedResizedImageOf(@NonNull String url, @NonNull String size) {
        return AvatarCache.buildUrl(url, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();
}
