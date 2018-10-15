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
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import org.jvnet.localizer.Localizable;

/**
 * Standard category for uncategorized instances.
 *
 * @since 2.0
 */
public final class UncategorizedSCMSourceCategory extends SCMSourceCategory {
    /**
     * The {@link UncategorizedSCMSourceCategory} singleton with the default naming.
     */
    public static final UncategorizedSCMSourceCategory DEFAULT = new UncategorizedSCMSourceCategory();

    /**
     * Constructs a {@link UncategorizedSCMSourceCategory} using the default naming.
     */
    private UncategorizedSCMSourceCategory() {
        super(Messages._UncategorizedSCMSourceCategory_DisplayName());
    }

    /**
     * Constructs a {@link UncategorizedSCMSourceCategory} with customized naming. Use this constructor when the generic
     * naming is not appropriate terminology for the specific {@link SCMNavigator}'s naming of repositories.
     * <p>For example: the Accurev source control system uses the term "depots" to refer to the same thing that
     * Git would call "repositories", it would confuse Accurev users if we called their "depots" as "repositories"
     * (especially as Accurev uses the term "repository" to refer to the Accurev server) so an
     * Accurev specific provider would use this constructor to generate a singleton with the "depots" name.
     * If there is a Git and Accurev navigator in the same context then
     * {@link SCMSourceCategory#collectAndSimplify(Iterable)} will contain an {@link UncategorizedSCMSourceCategory} under
     * the {@code default} key that has a {@link #getDisplayName()} of {@code Depots / Repositories} (which may indeed
     * confuse the Accurev users who have not been exposed to Git, but as both are in use they should have
     * been prepared for the different terminology)
     *
     * @param displayName the display name for the uncategorized {@link SCMSource}s when the source control system uses
     *                    a different terminology from "repositories".
     */
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
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
