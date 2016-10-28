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
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import org.jvnet.localizer.Localizable;

/**
 * Standard category for uncategorized instances.
 *
 * @since FIXME
 */
public final class UncategorizedSCMSourceCategory extends SCMSourceCategory {
    /**
     * Constructs a {@link UncategorizedSCMSourceCategory} using the default naming.
     */
    public UncategorizedSCMSourceCategory() {
        super(Messages._UncategorizedSCMSourceCategory_DisplayName());
    }

    /**
     * Constructs a {@link UncategorizedSCMSourceCategory} with customized naming. Use this constructor when the generic
     * naming is not appropriate terminology for the specific {@link SCMNavigator}'s naming of repositories.
     *
     * @param displayName the display name for change requests.
     */
    public UncategorizedSCMSourceCategory(@NonNull Localizable displayName) {
        super(displayName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatch(@NonNull SCMSource instance) {
        return true;
    }
}
