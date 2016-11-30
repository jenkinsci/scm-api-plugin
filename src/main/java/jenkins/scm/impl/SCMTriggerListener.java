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

package jenkins.scm.impl;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMEventListener;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.triggers.SCMTriggerItem;

/**
 * A {@link SCMEventListener} that will trigger the post commit hooks enabled by {@link SCMTrigger} for any
 * {@link SCMHeadEvent} which return a positive match against a {@link SCM} through {@link SCMHeadEvent#isMatch(SCM)}.
 *
 * @since 2.0
 */
@Extension
public class SCMTriggerListener extends SCMEventListener {
    /**
     * Our logger
     */
    private static final Logger LOGGER = Logger.getLogger(SCMTriggerListener.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSCMHeadEvent(SCMHeadEvent<?> event) {
        switch (event.getType()) {
            case CREATED:
            case UPDATED:
                // only trigger polling for create/update
                for (Item project : Jenkins.getActiveInstance().getAllItems()) {
                    SCMTriggerItem scmTriggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(project);
                    if (scmTriggerItem == null) {
                        // if it is not a SCMTriggerItem => ignore it
                        continue;
                    }
                    SCMTrigger trigger = scmTriggerItem.getSCMTrigger();
                    if (trigger == null || trigger.isIgnorePostCommitHooks()) {
                        // if it is not enabled for triggering => ignore it
                        continue;
                    }
                    for (SCM scm : scmTriggerItem.getSCMs()) {
                        if (!event.isMatch(scm)) {
                            // only interested in SCMs that match the event
                            LOGGER.log(Level.INFO, "Triggering polling of {0}", project.getFullName());
                            Cause[] causes = event.asCauses();
                            trigger.run(causes.length == 0 ? null : new Action[]{new CauseAction(causes)});
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
