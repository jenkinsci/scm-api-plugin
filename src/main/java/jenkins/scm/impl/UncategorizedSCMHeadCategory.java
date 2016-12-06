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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;
import org.jvnet.localizer.Localizable;

/**
 * Standard category for uncategorized instances.
 *
 * @since 2.0
 */
public final class UncategorizedSCMHeadCategory extends SCMHeadCategory {
    /**
     * The {@link UncategorizedSCMHeadCategory} singleton with the default naming.
     */
    public static final UncategorizedSCMHeadCategory DEFAULT = new UncategorizedSCMHeadCategory();

    /**
     * Constructs a {@link UncategorizedSCMHeadCategory} using the default naming.
     */
    private UncategorizedSCMHeadCategory() {
        super(Messages._UncategorizedSCMHeadCategory_DisplayName());
    }

    /**
     * Constructs a {@link UncategorizedSCMHeadCategory} with customized naming. Use this constructor when the generic
     * naming is not appropriate terminology for the specific {@link SCMSource}'s naming of branches.
     * <p>For example: the Accurev source control system uses the term "streams" to refer to the same thing that
     * Git would call "branches", it would confuse Accurev users if we called their "streams" as "branches" so an
     * Accurev specific provider would use this constructor to generate a singleton with the "streams" name.
     * If there is a Git and Accurev source in the same context then
     * {@link SCMHeadCategory#collectAndSimplify(Iterable)} will contain an {@link UncategorizedSCMHeadCategory} under
     * the {@code default} key that has a {@link #getDisplayName()} of {@code Branches / Streams}
     *
     * @param displayName the display name for the uncategorized {@link SCMHead}s when the source control system uses a
     *                    different terminology from "branches".
     */
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
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
