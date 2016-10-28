/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import java.util.ArrayList;
import java.util.List;
import jenkins.scm.api.actions.ChangeRequestAction;

/**
 * Base class for {@link SCMHead} instances that correspond to a change request.
 *
 * @since FIXME
 */
public abstract class ChangeRequestSCMHead extends SCMHead {
    /**
     * Constructor.
     *
     * @param name the name.
     */
    public ChangeRequestSCMHead(@NonNull String name) {
        super(name);
    }

    /**
     * Returns the {@link ChangeRequestAction} of this {@link ChangeRequestSCMHead}.
     *
     * @return the {@link ChangeRequestAction} of this {@link ChangeRequestSCMHead}.
     */
    @NonNull
    public abstract ChangeRequestAction getChangeRequestAction();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Action> getAllActions() {
        List<Action> actions = new ArrayList<Action>(super.getAllActions());
        actions.add(getChangeRequestAction());
        return actions;
    }
}
