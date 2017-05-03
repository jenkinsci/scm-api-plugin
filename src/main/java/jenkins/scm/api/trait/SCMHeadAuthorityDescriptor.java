/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Descriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.SCMHeadMixin;
import org.jvnet.tiger_types.Types;

/**
 * {@link Descriptor} base class for {@link SCMHeadAuthority} implementations.
 *
 * @since 3.4.0
 */
public abstract class SCMHeadAuthorityDescriptor extends Descriptor<SCMHeadAuthority<?, ?, ?>> {

    /**
     * The {@link SCMSourceRequest} class.
     */
    private final Class<? extends SCMSourceRequest> requestClass;
    /**
     * The {@link SCMHeadMixin} class.
     */
    private final Class<? extends SCMHeadMixin> headClass;
    /**
     * The {@link SCMRevision} class.
     */
    private final Class<? extends SCMRevision> revisionClass;

    /**
     * Constructor to use when type inferrence using {@link #SCMHeadAuthorityDescriptor()} does not work.
     *
     * @param clazz         Pass in the type of {@link SCMHeadAuthority}
     * @param requestClass  the type of {@link SCMSourceRequest}.
     * @param headClass     the type of {@link SCMHead}.
     * @param revisionClass the type of {@link SCMRevision}.
     */
    protected SCMHeadAuthorityDescriptor(Class<? extends SCMHeadAuthority<?, ?, ?>> clazz,
                                         Class<? extends SCMSourceRequest> requestClass,
                                         Class<? extends SCMHeadMixin> headClass,
                                         Class<? extends SCMRevision> revisionClass) {
        super(clazz);
        this.requestClass = requestClass;
        this.headClass = headClass;
        this.revisionClass = revisionClass;
    }

    /**
     * Infers the type of the corresponding {@link SCMHeadAuthority} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     */
    protected SCMHeadAuthorityDescriptor() {
        Type bt = Types.getBaseClass(clazz, SCMHeadAuthority.class);
        if (bt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) bt;
            // this 'headClass' is the closest approximation of T of SCMHeadAuthority<T>.
            requestClass = Types.erasure(pt.getActualTypeArguments()[0]);
            headClass = Types.erasure(pt.getActualTypeArguments()[1]);
            revisionClass = Types.erasure(pt.getActualTypeArguments()[2]);
            if (!SCMSourceRequest.class.isAssignableFrom(requestClass)) {
                throw new AssertionError(
                        "Failed to correctly detect SCMSourceRequest specialization. Use the constructor that takes "
                                + "the "
                                + "Class objects explicitly");
            }
            if (!SCMHeadMixin.class.isAssignableFrom(headClass)) {
                throw new AssertionError(
                        "Failed to correctly detect SCMHead specialization. Use the constructor that takes the Class "
                                + "objects explicitly");
            }
            if (!SCMRevision.class.isAssignableFrom(revisionClass)) {
                throw new AssertionError(
                        "Failed to correctly detect SCMRevision specialization. Use the constructor that takes the "
                                + "Class "
                                + "objects explicitly");
            }
        } else {
            throw new AssertionError(
                    "Failed to correctly detect specialization. Use the constructor that takes the Class objects "
                            + "explicitly");
        }
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied {@link SCMHead}.
     *
     * @param head the {@link SCMHead}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToHead(@NonNull SCMHead head) {
        return isApplicableToHead(head.getClass()) && isApplicableToOrigin(head.getOrigin());
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied type of {@link SCMHead}.
     *
     * @param headClass the type of {@link SCMHead}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToHead(@NonNull Class<? extends SCMHeadMixin> headClass) {
        return this.headClass.isAssignableFrom(headClass);
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied {@link SCMRevision}.
     *
     * @param revision the {@link SCMRevision}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToRevision(@NonNull SCMRevision revision) {
        return isApplicableToHead(revision.getHead()) && isApplicableToRevision(revision.getClass());
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied type of {@link SCMRevision}.
     *
     * @param revisionClass the type of {@link SCMRevision}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToRevision(@NonNull Class<? extends SCMRevision> revisionClass) {
        return this.revisionClass.isAssignableFrom(revisionClass);
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied {@link SCMSourceRequest}.
     *
     * @param request the {@link SCMSourceRequest}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToRequest(@NonNull SCMSourceRequest request) {
        return requestClass.isInstance(request);
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied type of {@link SCMSourceRequest}.
     *
     * @param requestClass the type of {@link SCMSourceRequest}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToRequest(@NonNull Class<? extends SCMSourceRequest> requestClass) {
        return this.requestClass.isAssignableFrom(requestClass);
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied {@link SCMHeadOrigin}.
     *
     * @param origin the {@link SCMHeadOrigin}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToOrigin(@NonNull SCMHeadOrigin origin) {
        return isApplicableToOrigin(origin.getClass());
    }

    /**
     * Checks if this {@link SCMHeadAuthorityDescriptor} is applicable to the supplied {@link SCMHeadOrigin}.
     *
     * @param originClass the type of {@link SCMHeadOrigin}.
     * @return {@code true} if applicable.
     */
    public boolean isApplicableToOrigin(@NonNull Class<? extends SCMHeadOrigin> originClass) {
        return true;
    }
}
