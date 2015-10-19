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

import hudson.model.Descriptor;
import hudson.model.TopLevelItemDescriptor;
import javax.annotation.CheckForNull;

/**
 * Definition of a kind of {@link SCMNavigator}.
 * @since FIXME
 */
public abstract class SCMNavigatorDescriptor extends Descriptor<SCMNavigator> {

    protected SCMNavigatorDescriptor() {}

    protected SCMNavigatorDescriptor(Class<? extends SCMNavigator> clazz) {
        super(clazz);
    }

    /**
     * Creates a default navigator, if there should be one displayed.
     * May be interpreted by {@code jenkins.branch.SpecificDescriptor}.
     * If returning non-null, you may also provide a {@code newInstanceDetail} view as per {@link TopLevelItemDescriptor}.
     * @param name a name supplied by the user which may be used as a hint for preconfiguration
     * @return a navigator with a default configuration, or null if it should not be advertised this way
     */
    @CheckForNull
    public abstract SCMNavigator newInstance(@CheckForNull String name);

}
