/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * An API for discovering new and navigating already discovered {@link SCMSource}s within an organization.
 * An implementation does not need to cache existing discoveries, but some form of caching is strongly recommended
 * where the backing provider of repositories has a rate limiter on API calls.
 * @since FIXME
 */
public abstract class SCMNavigator extends AbstractDescribableImpl<SCMNavigator> implements ExtensionPoint {

    protected SCMNavigator() {}

    /**
     * Looks for SCM sources in a configured place.
     * @param context who is asking
     * @param listener a listener to be notified of progress
     * @return a map from project names, to a list of potential SCM sources as in {@code MultiBranchProject.getSCMSources}; do not call {@link SCMSource#setOwner} on them
     * @throws IOException if scanning fails
     * @throws InterruptedException if scanning is interrupted
     */
    public abstract @Nonnull Map<String,? extends List<? extends SCMSource>> discoverSources(@Nonnull SCMSourceOwner context, @Nonnull TaskListener listener) throws IOException, InterruptedException;

    // TODO this probably needs some way of customizing projects that get created, for example to add a link to GitHub in the description

    @Override public SCMNavigatorDescriptor getDescriptor() {
        return (SCMNavigatorDescriptor) super.getDescriptor();
    }

}
