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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import java.io.Serializable;
import jenkins.model.Jenkins;

/**
 * Represents a named SCM branch, tag or mainline.
 *
 * @author Stephen Connolly
 */
public class SCMHead implements Comparable<SCMHead>, Serializable {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name.
     */
    @NonNull
    private final String name;

    /**
     * Constructor.
     *
     * @param name the name.
     */
    public SCMHead(@NonNull String name) {
        name.getClass(); // throw NPE if null
        this.name = name;
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    @NonNull
    public String getName() {
        return name;
    }

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

        SCMHead scmHead = (SCMHead) o;

        if (!name.equals(scmHead.name)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(SCMHead o) {
        return getName().compareTo(o.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SCMHead{'");
        sb.append(name).append("'}");
        return sb.toString();
    }

    /**
     * Means of locating a head given an item.
     * @since FIXME
     */
    public static abstract class HeadByItem implements ExtensionPoint {

        /**
         * Checks whether a given item corresponds to a particular SCM head.
         * @param item such as a {@linkplain ItemGroup#getItems child} of an {@link SCMSourceOwner}
         * @return a corresponding SCM head, or null if this information is unavailable
         * @since FIXME
         */
        @CheckForNull
        public abstract SCMHead getHead(Item item);

        /**
         * Runs all registered implementations.
         * @param item an item, such as a branch project
         * @return the corresponding head, if known
         */
        @CheckForNull
        public static SCMHead findHead(Item item) {
            for (HeadByItem ext : ExtensionList.lookup(HeadByItem.class)) {
                SCMHead head = ext.getHead(item);
                if (head != null) {
                    return head;
                }
            }
            return null;
        }

    }

}
