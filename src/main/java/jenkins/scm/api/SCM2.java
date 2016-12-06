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

import hudson.model.Action;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import java.util.Collection;
import java.util.Collections;
import jenkins.scm.impl.SCM2Notifier;
import jenkins.scm.impl.SCM2TransientActionFactory;
import jenkins.scm.impl.SCMTriggerListener;
import jenkins.triggers.SCMTriggerItem;

/**
 * Extended functionality base class for {@link SCM} implementations that only want to handle one event system and
 * are consolidating on the SCM API event system. It is not strictly required to extend from this class in place of
 * {@link SCM} but typically implementations would need to have the functionality made available from
 * {@link #afterSave(SCMTriggerItem)} and/or {@link #getItemActions(SCMTriggerItem)}. Implementations that do
 * not require these features can get integration with the SCM API event subsystem by implementing
 * {@link SCMHeadEvent#isMatch(SCM)}.
 *
 * @since 2.0
 * @see SCM2TransientActionFactory
 * @see SCM2Notifier
 * @see SCMHeadEvent#isMatch(SCM)
 */
// TODO migrate these new API methods into hudson.model.SCM
public abstract class SCM2 extends SCM {
    /**
     * Callback from the {@link SCMTriggerItem} after the {@link SCMTriggerItem} has been saved. Can be used to
     * register the {@link SCMTriggerItem} for a call-back hook from the backing SCM that this source is for.
     * Implementations are responsible for ensuring that they do not create duplicate registrations and that orphaned
     * registrations are removed eventually.
     *
     * @param owner the owner of this {@link SCM2}.
     * @see SCMTriggerListener
     * @see SCMTrigger#isIgnorePostCommitHooks()
     * @see SCMHeadEvent#isMatch(SCM)
     */
    public void afterSave(SCMTriggerItem owner) {}

    /**
     * {@link Action}s to be displayed in the {@link SCMTriggerItem} page.
     *
     * @param owner the owner of this {@link SCM2}.
     * @return can be empty but never null
     */
    public Collection<? extends Action> getItemActions(SCMTriggerItem owner) {
        return Collections.emptyList();
    }
}
