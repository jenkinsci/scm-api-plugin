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
import hudson.ExtensionList;
import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import jenkins.scm.api.SCM2;
import jenkins.triggers.SCMTriggerItem;

/**
 * This class is responsible for firing the {@link jenkins.scm.api.SCM2#afterSave(SCMTriggerItem)} event.
 */
@Extension
public class SCM2Notifier extends SaveableListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChange(Saveable o, XmlFile file) {
        if (!(o instanceof Item)) {
            // must be an Item
            return;
        }
        SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem((Item) o);
        if (item == null) {
            // more specifically must be an SCMTriggerItem
            return;
        }
        SCMTrigger trigger = item.getSCMTrigger();
        if (trigger == null || trigger.isIgnorePostCommitHooks()) {
            // must have the trigger enabled and not opted out of post commit hooks
            return;
        }
        for (SCM scm : item.getSCMs()) {
            if (scm instanceof SCM2) {
                // we have a winner
                ((SCM2) scm).afterSave(item);
            }
        }
    }
}
