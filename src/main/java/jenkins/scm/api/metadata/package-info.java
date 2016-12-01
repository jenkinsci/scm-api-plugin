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
 *
 */

/**
 * The {@linkplain hudson.model.InvisibleAction metadata} classes that can be used to report metadata about
 * {@linkplain jenkins.scm.api.SCMNavigator navigator}, {@linkplain jenkins.scm.api.SCMSource source},
 * {@linkplain jenkins.scm.api.SCMHead head} and {@linkplain jenkins.scm.api.SCMRevision revisions} objects via the
 * {@link jenkins.scm.api.SCMNavigator#fetchActions(jenkins.scm.api.SCMNavigatorOwner, jenkins.scm.api.SCMNavigatorEvent, hudson.model.TaskListener) SCMNavigator.fetchActions(...)},
 * {@link jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMSourceEvent, hudson.model.TaskListener) SCMSource.fetchActions(...)},
 * {@link jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMHead, jenkins.scm.api.SCMHeadEvent, hudson.model.TaskListener) SCMSource.fetchActions(SCMHead, ...)}
 * and
 * {@link jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMRevision, jenkins.scm.api.SCMHeadEvent, hudson.model.TaskListener) SCMSource.fetchActions(SCMRevision, ...)}
 * methods respectively.
 *
 * @see hudson.model.InvisibleAction
 * @see jenkins.scm.api.SCMNavigator#fetchActions(jenkins.scm.api.SCMNavigatorOwner, jenkins.scm.api.SCMNavigatorEvent, hudson.model.TaskListener)
 * @see jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMSourceEvent, hudson.model.TaskListener)
 * @see jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMHead, jenkins.scm.api.SCMHeadEvent, hudson.model.TaskListener)
 * @see jenkins.scm.api.SCMSource#fetchActions(jenkins.scm.api.SCMRevision, jenkins.scm.api.SCMHeadEvent, hudson.model.TaskListener)
 * @since 2.0
 */
package jenkins.scm.api.metadata;
