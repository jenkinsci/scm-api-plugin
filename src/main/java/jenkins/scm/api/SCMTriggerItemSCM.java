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

package jenkins.scm.api;

import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import jenkins.scm.impl.SCMTriggerItemSCMNotifier;
import jenkins.scm.impl.SCMTriggerListener;
import jenkins.triggers.SCMTriggerItem;
import org.apache.tools.ant.ExtensionPoint;

/**
 * Extension point to allow modern {@link SCM} implementations to auto-register call-back hooks for
 * {@link SCMTriggerItem} instaces without having to implement a custom {@link Trigger}.
 *
 * @since 2.0
 * @see SCMTriggerItemSCMNotifier
 */
public abstract class SCMTriggerItemSCM extends ExtensionPoint {

    /**
     * Tests if this listener applies to the supplied {@link SCM}.
     *
     * @param scm the {@link SCM}.
     * @return {@code true} if and only if this listener is interested in the supplied {@link SCM}.
     * @see SCMTriggerListener
     * @see SCMTrigger#isIgnorePostCommitHooks()
     * @see SCMHeadEvent#isMatch(SCM)
     */
    public abstract boolean isMatch(SCM scm);

    /**
     * Callback from the {@link SCMTriggerItem} after the {@link SCMTriggerItem} has been saved. Can be used to
     * register the {@link SCMTriggerItem} for a call-back hook from the backing SCM that this source is for.
     *
     * @param item the item that was saved.
     * @param scm the {@link SCM} instance from the item that {@link #isMatch(SCM)}.
     * @see SCMTriggerListener
     * @see SCMTrigger#isIgnorePostCommitHooks()
     * @see SCMHeadEvent#isMatch(SCM)
     */
    public abstract void afterSave(SCMTriggerItem item, SCM scm);
}
