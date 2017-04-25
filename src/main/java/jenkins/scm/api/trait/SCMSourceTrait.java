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
 *
 */

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.LogTaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMBuilder;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadObserver;


public class SCMSourceTrait extends AbstractDescribableImpl<SCMSourceTrait> {

    public final <B extends SCMSourceRequestBuilder<B, R>, R extends SCMSourceRequest> void applyToRequest(B builder) {
        if (getDescriptor().isApplicableTo(builder.getClass())) {
            // guard against non-applicable
            decorateRequest(builder);
        }
    }

    protected <B extends SCMSourceRequestBuilder<B, R>, R extends SCMSourceRequest> void decorateRequest(B builder) {
    }

    @NonNull
    public final SCMHeadObserver applyToObserver(@NonNull SCMHeadObserver observer) {
        return decorateObserver(observer);
    }

    @NonNull
    public SCMHeadObserver decorateObserver(@NonNull SCMHeadObserver observer) {
        return observer;
    }

    public final <B extends SCMBuilder<B, S>, S extends SCM> void applyToSCM(B builder) {
        if (!getDescriptor().isApplicableTo(builder.getSCMDescriptor())) {
            // guard against non-applicable
        }
        decorateSCM(builder);
    }

    protected <B extends SCMBuilder<B, S>, S extends SCM> void decorateSCM(B builder) {
    }

    public boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        return true;
    }

    @Override
    public SCMSourceTraitDescriptor getDescriptor() {
        return (SCMSourceTraitDescriptor) super.getDescriptor();
    }

    /**
     * Turns a possibly {@code null} {@link TaskListener} reference into a guaranteed non-null reference.
     *
     * @param listener a possibly {@code null} {@link TaskListener} reference.
     * @return guaranteed non-null {@link TaskListener}.
     */
    @NonNull
    private TaskListener defaultListener(@CheckForNull TaskListener listener) {
        if (listener == null) {
            Level level;
            try {
                level = Level.parse(System.getProperty(getClass().getName() + ".defaultListenerLevel", "FINE"));
            } catch (IllegalArgumentException e) {
                level = Level.FINE;
            }
            return new LogTaskListener(Logger.getLogger(getClass().getName()), level);
        }
        return listener;
    }

}
