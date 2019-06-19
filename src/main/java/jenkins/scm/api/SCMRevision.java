/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
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

import java.io.Serializable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Base class that represents a specific (or not so specific) revision of a {@link SCMHead}.
 *
 * @author Stephen Connolly
 */
@ExportedBean
public abstract class SCMRevision implements Serializable {

    /**
     * The {@link SCMHead} that this revision belongs to.
     */
    @NonNull
    private final SCMHead head;

    /**
     * Constructor.
     *
     * @param head the {@link SCMHead} that the {@link SCMRevision} belongs to.
     */
    protected SCMRevision(@NonNull SCMHead head) {
        head.getClass(); // throw NPE if null
        this.head = head;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(Object obj); // force implementers to implement.

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();  // force implementers to implement.

    /**
     * Should provide a concise, human-readable summary of this revision in an implementation-dependent format.
     * <p>{@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns {@code true} if and only if this revision is deterministic, in other words that repeated checkouts of
     * this revision will result in the exact same files being checked out. Most modern SCM systems have a deterministic
     * revision, however some of the older ones do not have a deterministic revision for all types of head.
     *
     * @return {@code true} if and only if this revision is deterministic.
     */
    @Exported
    public boolean isDeterministic() {
        return true;
    }

    /**
     * Returns the {@link SCMHead} that this {@link SCMRevision} belongs to.
     *
     * @return the {@link SCMHead} that this {@link SCMRevision} belongs to.
     */
    @Exported
    @NonNull
    public final SCMHead getHead() {
        return head;
    }
}
