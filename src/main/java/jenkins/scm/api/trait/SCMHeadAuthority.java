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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;
import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.SCMHeadMixin;

/**
 * Abstraction to allow pluggable definitions of trust for {@link SCMHead} and {@link SCMRevision} instances in the
 * context of a specific {@link SCMSourceRequest}.
 * <p>
 * Note: there can be multiple authorities for the same types of head / revision active in the context of any one
 * request. The ultimate trust state is determined by a logical OR operation, in other words if any one authority
 * says that the head / revision is trusted then the head / revision is trusted.
 *
 * @param <S> the type of {@link SCMSourceRequest}.
 * @param <H> the specialization of {@link SCMHeadMixin} that this authority provides trust information for.
 * @param <R> the specialization of {@link SCMRevision} that this authority provides trust information for.
 * @since 3.4.0
 */
public abstract class SCMHeadAuthority<S extends SCMSourceRequest, H extends SCMHeadMixin, R extends SCMRevision>
        extends AbstractDescribableImpl<SCMHeadAuthority<?, ?, ?>> {

    /**
     * Checks if this authority is relevant to the supplied {@link SCMHead}.
     *
     * @param head the supplied {@link SCMHead}.
     * @return {@code true} if and only if this authority can provide trust information for the supplied head.
     */
    public final boolean isApplicableTo(@NonNull SCMHead head) {
        return getDescriptor().isApplicableToHead(head);
    }

    /**
     * Checks if this authority is relevant to the supplied {@link SCMRevision}.
     *
     * @param revision the supplied {@link SCMRevision}.
     * @return {@code true} if and only if this authority can provide trust information for the supplied revision.
     */
    public final boolean isApplicableTo(@NonNull SCMRevision revision) {
        return getDescriptor().isApplicableToRevision(revision);
    }

    /**
     * Checks if this authority is relevant to the supplied {@link SCMSourceRequest}.
     *
     * @param request the supplied {@link SCMSourceRequest}.
     * @return {@code true} if and only if this authority can provide trust information for the supplied request.
     */
    public final boolean isApplicableTo(@NonNull SCMSourceRequest request) {
        return getDescriptor().isApplicableToRequest(request);
    }

    /**
     * Checks if the supplied {@link SCMHead} is trusted in the context of the specified {@link SCMSourceRequest}.
     *
     * @param request the {@link SCMSourceRequest}.
     * @param head    the {@link SCMHead}.
     * @return {@code true} if the supplied head is trusted.
     * @throws IOException          if there was an I/O error trying to establish the trust status.
     * @throws InterruptedException if interrupted while trying to establing the trust status.
     */
    @SuppressWarnings("unchecked")
    public final boolean isTrusted(@NonNull SCMSourceRequest request, @NonNull SCMHead head)
            throws IOException, InterruptedException {
        return isApplicableTo(request)
                && isApplicableTo(head)
                && checkTrusted((S) request, (H) head);
    }

    /**
     * Checks if the supplied {@link SCMRevision} is trusted in the context of the specified {@link SCMSourceRequest}.
     *
     * @param request  the {@link SCMSourceRequest}.
     * @param revision the {@link SCMRevision}.
     * @return {@code true} if the supplied revision is trusted.
     * @throws IOException          if there was an I/O error trying to establish the trust status.
     * @throws InterruptedException if interrupted while trying to establing the trust status.
     */
    @SuppressWarnings("unchecked")
    public final boolean isTrusted(@NonNull SCMSourceRequest request, @CheckForNull SCMRevision revision)
            throws IOException, InterruptedException {
        return isApplicableTo(request)
                && isApplicableTo(revision.getHead())
                && isApplicableTo(revision)
                && checkTrusted((S) request, (R) revision);
    }

    /**
     * SPI: checks if the supplied {@link SCMHead} is trusted in the context of the supplied {@link SCMSourceRequest}.
     *
     * @param request the {@link SCMSourceRequest}.
     * @param head    the {@link SCMHead}.
     * @return {@code true} if trusted.
     * @throws IOException          if there was an I/O error trying to establish the trust status.
     * @throws InterruptedException if interrupted while trying to establing the trust status.
     */
    protected abstract boolean checkTrusted(@NonNull S request, @NonNull H head) throws IOException, InterruptedException;

    /**
     * SPI: checks if the supplied {@link SCMRevision} is trusted in the context of the supplied
     * {@link SCMSourceRequest}. Default implementation just calls {@link #checkTrusted(SCMSourceRequest, SCMHeadMixin)}
     * with {@link SCMRevision#getHead()}.
     *
     * @param request  the {@link SCMSourceRequest}.
     * @param revision the {@link SCMRevision}.
     * @return {@code true} if trusted.
     * @throws IOException          if there was an I/O error trying to establish the trust status.
     * @throws InterruptedException if interrupted while trying to establing the trust status.
     */
    @SuppressWarnings("unchecked")
    @OverrideMustInvoke
    protected boolean checkTrusted(@NonNull S request, @NonNull R revision) throws IOException, InterruptedException {
        return checkTrusted(request, (H) revision.getHead());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SCMHeadAuthorityDescriptor getDescriptor() {
        return (SCMHeadAuthorityDescriptor) super.getDescriptor();
    }

    /**
     * Returns a list of all {@link SCMHeadAuthorityDescriptor} instances.
     *
     * @return a list of all {@link SCMHeadAuthorityDescriptor} instances.
     */
    @SuppressWarnings("unchecked")
    public static DescriptorExtensionList<SCMHeadAuthority<?, ?, ?>, SCMHeadAuthorityDescriptor> all() {
        return (DescriptorExtensionList) Jenkins.get().getDescriptorList(SCMHeadAuthority.class);
    }

    /**
     * Returns the subset of {@link SCMHeadAuthorityDescriptor} instances applicable to the supplied criteria.
     *
     * @param requestClass  the {@link SCMSourceRequest} class (or {@code null} to match any).
     * @param headClass     the {@link SCMHeadMixin} class (or {@code null} to match any).
     * @param revisionClass the {@link SCMRevision} class (or {@code null} to match any).
     * @param origins       the {@link SCMHeadOrigin} instances.
     * @return the list of matching {@link SCMHeadAuthorityDescriptor} instances.
     */
    public static List<SCMHeadAuthorityDescriptor> _for(@CheckForNull Class<? extends SCMSourceRequest> requestClass,
                                                        @CheckForNull Class<? extends SCMHeadMixin> headClass,
                                                        @CheckForNull Class<? extends SCMRevision> revisionClass,
                                                        Class<? extends SCMHeadOrigin>... origins) {
        if (requestClass == null) {
            requestClass = SCMSourceRequest.class;
        }
        if (headClass == null) {
            headClass = SCMHeadMixin.class;
        }
        if (revisionClass == null) {
            revisionClass = SCMRevision.class;
        }
        List<SCMHeadAuthorityDescriptor> result = new ArrayList<SCMHeadAuthorityDescriptor>();
        for (SCMHeadAuthorityDescriptor d : all()) {
            if (d.isApplicableToRequest(requestClass)
                    && d.isApplicableToHead(headClass)
                    && d.isApplicableToRevision(revisionClass)) {
                boolean match = origins.length == 0;
                if (!match) {
                    for (Class<? extends SCMHeadOrigin> o : origins) {
                        if (d.isApplicableToOrigin(o)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    result.add(d);
                }
            }
        }
        return result;
    }

}
