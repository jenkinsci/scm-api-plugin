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
 */

package jenkins.scm.api.mixin;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.kohsuke.stapler.export.Exported;

/**
 * Recommended base class for the {@link SCMRevision} of a {@link ChangeRequestSCMHead}.
 *
 * @since 2.2.0
 */
public abstract class ChangeRequestSCMRevision<H extends SCMHead & ChangeRequestSCMHead> extends SCMRevision {
    /**
     * The revision of the {@link ChangeRequestSCMHead#getTarget()}.
     */
    private final SCMRevision target;

    /**
     * Constructor.
     *
     * @param head   the {@link SCMHead} that the {@link SCMRevision} belongs to.
     * @param target the {@link SCMRevision} of the {@link ChangeRequestSCMHead#getTarget()}.
     */
    protected ChangeRequestSCMRevision(@NonNull H head, @NonNull SCMRevision target) {
        super(head);
        if (!(target.getHead().equals(head.getTarget()))) {
            throw new IllegalArgumentException("The target revision's head must correspond to the heads target");
        }
        this.target = target;
    }

    /**
     * Returns the revision of the {@link ChangeRequestSCMHead#getTarget()} that this {@link ChangeRequestSCMHead} is
     * associated with.
     *
     * @return a “target” or “base” branch revision.
     */
    @Exported
    @NonNull
    public final SCMRevision getTarget() {
        return target;
    }

    /**
     * Is this a {@link ChangeRequestSCMRevision} that will be merged on top of {@link #getTarget()}?
     *
     * @return {@code true} if the effective revision is the result of merging onto the {@link #getTarget()}
     * revision {@code false} if the effective revision ignores the {@link #getTarget()}.
     * @see MergeableChangeRequestSCMHead
     */
    public final boolean isMerge() {
        SCMHead head = getHead();
        return !(head instanceof MergeableChangeRequestSCMHead) || ((MergeableChangeRequestSCMHead) head).isMerge();
    }

    /**
     * Performs an equality comparison as for {@link #equals(Object)} but excludes the {@link #getTarget()}
     * {@link SCMRevision} from the comparison (though the {@link ChangeRequestSCMHead#getTarget()} will be included
     * as part of the {@link #getHead()} comparison.
     *
     * @param revision the revision to compare with.
     * @return {@code true} if equal to supplied revision (ignoring differences in {@link #getTarget()})
     */
    public abstract boolean equivalent(ChangeRequestSCMRevision<?> revision);

    /**
     * Compute the {@link #hashCode()} excluding {@link #getTarget()}.
     * @return a hash code value for this object.
     */
    protected abstract int _hashCode();

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChangeRequestSCMRevision<?> that = (ChangeRequestSCMRevision<?>) o;
        if (!equivalent(that)) {
            return false;
        }
        return !isMerge() || target.equals(that.target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        int rv = _hashCode();
        if (isMerge()) {
            rv = rv * 31 + target.hashCode();
        }
        return rv;
    }
}
