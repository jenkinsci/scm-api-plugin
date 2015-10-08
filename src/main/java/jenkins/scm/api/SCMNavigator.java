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
import java.io.IOException;
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
     * After this method completes, no further calls may be made to the {@code observer} or its child callbacks.
     * @param observer a recipient of progress notifications and a source of contextual information
     * @throws IOException if scanning fails
     * @throws InterruptedException if scanning is interrupted
     */
    public abstract void visitSources(@Nonnull SCMSourceObserver observer) throws IOException, InterruptedException;

    @Override public SCMNavigatorDescriptor getDescriptor() {
        return (SCMNavigatorDescriptor) super.getDescriptor();
    }

}
