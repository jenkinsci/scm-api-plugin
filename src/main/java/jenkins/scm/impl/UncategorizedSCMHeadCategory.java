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

package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import org.jvnet.localizer.Localizable;

/**
 * Standard category for uncategorized instances.
 *
 * @since FIXME
 */
public final class UncategorizedSCMHeadCategory extends SCMHeadCategory {
    /**
     * Constructs a {@link UncategorizedSCMHeadCategory} using the default naming.
     */
    public UncategorizedSCMHeadCategory() {
        super(Messages._UncategorizedSCMHeadCategory_DisplayName());
    }

    /**
     * Constructs a {@link UncategorizedSCMHeadCategory} with customized naming. Use this constructor when the generic
     * naming is not appropriate terminology for the specific {@link SCMSource}'s naming of branches
     *
     * @param displayName the display name for change requests.
     */
    public UncategorizedSCMHeadCategory(@NonNull Localizable displayName) {
        super(displayName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatch(@NonNull SCMHead instance) {
        return true;
    }
}
