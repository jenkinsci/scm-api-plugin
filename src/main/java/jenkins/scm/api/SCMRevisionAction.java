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
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;

/**
 * {@link Action} added to {@link AbstractBuild} to remember
 * which revision is built in the given build.
 *
 * @author Stephen Connolly
 */
public class SCMRevisionAction extends InvisibleAction {
    /**
     * The {@link SCMRevision}.
     */
    @NonNull
    private final SCMRevision revision;

    /**
     * Constructor.
     *
     * @param rev the {@link SCMRevision}.
     */
    public SCMRevisionAction(@NonNull SCMRevision rev) {
        rev.getClass(); // fail fast if null
        this.revision = rev;
    }

    /**
     * Gets the {@link SCMRevision}.
     *
     * @return the {@link SCMRevision}.
     */
    @NonNull
    public SCMRevision getRevision() {
        return revision;
    }

    /**
     * Gets the {@link SCMRevision} from the specified {@link Actionable}.
     *
     * @return the {@link SCMRevision}.
     */
    @CheckForNull
    public static SCMRevision getRevision(@NonNull Actionable actionable) {
        SCMRevisionAction action = actionable.getAction(SCMRevisionAction.class);
        return action != null ? action.getRevision() : null;
    }
}
