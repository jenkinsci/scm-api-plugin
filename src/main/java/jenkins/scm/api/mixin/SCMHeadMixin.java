/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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

package jenkins.scm.api.mixin;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.Serializable;
import jenkins.scm.api.SCMHead;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.export.Exported;

/**
 * Interface to allow declaring mixin interfaces for {@link SCMHead} subclasses. Do not implement this interface
 * directly, rather extend from {@link SCMHead} and implement the appropriate mixins such as
 * {@link ChangeRequestSCMHead} and {@link TagSCMHead}
 * <p>
 * Two {@link SCMHeadMixin} implementations are equal if and only if:
 * <ul>
 *     <li>They both are the same class</li>
 *     <li>They both have the same {@link SCMHeadMixin#getName()}</li>
 *     <li>For each implemented {@link SCMHeadMixin} sub-interface, they both return the same values from all Java
 *     Bean property getters declared on the sub-interface. Thus, for example {@link ChangeRequestSCMHead}
 *     implementations are only considered equal if {@link ChangeRequestSCMHead#getId()} and
 *     {@link ChangeRequestSCMHead#getTarget()} are also equal</li>
 * </ul>
 * The {@link Object#hashCode()} for a {@link SCMHeadMixin} implementation must be equal to the
 *     {@link String#hashCode()} of {@link SCMHeadMixin#getName()}
 *
 * @since 2.0
 */
public interface SCMHeadMixin extends Comparable<SCMHead>, Serializable {
    /**
     * Returns the name.
     *
     * @return the name.
     */
    @Exported
    @NonNull
    String getName();

    @Restricted(NoExternalUse.class)
    interface Equality {
        boolean equals(@NonNull SCMHeadMixin o1, @NonNull SCMHeadMixin o2);
    }
}
