/*
 * The MIT License
 *
 * Copyright (c) 2017 CloudBees, Inc.
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

import hudson.model.InvisibleAction;
import hudson.model.TaskListener;
import java.io.Serializable;
import jenkins.scm.api.SCMCategory;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;

/**
 * Identifies a {@link SCMHead} / {@link SCMSource} as being a primary instance. Some examples of how this metadata is
 * intended to be used:
 * <ul>
 * <li>The Git source control system
 * allows optionally identifying a specific branch as being the default branch. A Git branch source could therefore
 * return an instance of this action for the corresponding {@link SCMHead} from
 * {@link SCMSource#fetchActions(SCMHead, SCMHeadEvent, TaskListener)} to indicate that it is the "primary" head.</li>
 * <li>The GitHub repository hosting service has the concepts of fork repositories associated with a primary repository.
 * One option is that a GitHub {@link SCMNavigator} could use this action to differentiate forks from their primary
 * whereby {@link SCMSource#fetchActions(SCMSourceEvent, TaskListener)} would return an instance of this action to
 * identify the primary source.
 * An alternative option is that a GitHub specific {@link SCMSource} could use {@link SCMCategory} to differentiate
 * the {@link SCMHead} instances that originate from a fork and use
 * {@link SCMSource#fetchActions(SCMHead, SCMHeadEvent, TaskListener)} to mark the primary branches from each "source".
 * </li>
 * <li>Other SCM systems may have the concept of multiple primary heads. For example it may be possible to identify
 * heads as being a primary for the {@code 1.x}, {@code 2.x}, and {@code 3.x} release lines. *
 * </li>
 * </ul>
 *
 * Consumers of the SCM API should assume that:
 * <ul>
 * <li>there can be zero, one or many {@link SCMHead} instances with this metadata</li>
 * <li>there can be zero, one or many {@link SCMSource} instances with this metadata</li>
 * <li>within any {@link SCMCategory} there can be zero, one or many {@link SCMHead}/{@link SCMSource}
 * instances with this metadata</li>
 * <li>the information conveyed by this metadata may or may not be orthogonal to the {@link SCMCategory}. Categories
 * convey pseudo-type information from the source / navigator. The categorizations that a source / navigator chooses
 * to implement may be such that they are orthogonal to primary instance or they may be complementary with primary
 * instance. The decision as to which of these is appropriate for a specific SCM system is out of scope for the SCM API.
 * </li>
 * </ul>
 *
 * @since 2.0.1
 */
public class PrimaryInstanceMetadataAction extends InvisibleAction implements Serializable {
    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

}
