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
 *
 */

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.AbstractDescribableImpl;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.SCMBuilder;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;

/**
 * Represents a trait of behaviour or configuration that can be applied to a {@link SCMSource}.
 *
 * @since 2.2.0
 */
public class SCMSourceTrait extends AbstractDescribableImpl<SCMSourceTrait> {

    /**
     * Applies this trait to the {@link SCMSourceContext}.
     *
     * @param context the context.
     */
    public final void applyToContext(SCMSourceContext<?, ?> context) {
        if (getDescriptor().isApplicableTo(context.getClass())) {
            // guard against non-applicable
            decorateContext((SCMSourceContext) context);
        }
    }

    /**
     * SPI: Override this method to decorate a {@link SCMSourceContext}. You can assume that your
     * {@link SCMSourceTraitDescriptor#isApplicableTo(Class)} is {@code true} within this method.
     *
     * @param context the context (invariant: {@link SCMSourceTraitDescriptor#isApplicableTo(Class)} is {@code true})
     * @param <B>     generic type parameter to ensure type information available.
     * @param <R>     generic type parameter to ensure type information available.
     */
    protected <B extends SCMSourceContext<B, R>, R extends SCMSourceRequest> void decorateContext(B context) {
    }

    /**
     * Applies this trait to an observer for use during a {@link SCMSourceRequest}.
     *
     * @param observer the observer.
     * @return the supplied observer or a wrapped variant of it.
     */
    @NonNull
    public final SCMHeadObserver applyToObserver(@NonNull SCMHeadObserver observer) {
        return decorateObserver(observer);
    }

    /**
     * SPI: Override this method to decorate the {@link SCMHeadObserver} used during a {@link SCMSourceRequest}.
     *
     * @param observer the observer.
     * @return the supplied observer or a wrapped variant of it.
     */
    @NonNull
    protected SCMHeadObserver decorateObserver(@NonNull SCMHeadObserver observer) {
        return observer;
    }

    /**
     * Applies this trait to the {@link SCMBuilder}.
     *
     * @param builder the builder.
     */
    public final void applyToBuilder(SCMBuilder<?, ?> builder) {
        if (!getDescriptor().isApplicableTo(builder.getSCMDescriptor())) {
            // guard against non-applicable
        }
        decorateBuilder((SCMBuilder) builder);
    }

    /**
     * SPI: Override this method to decorate a {@link SCMBuilder}. You can assume that your
     * {@link SCMSourceTraitDescriptor#isApplicableTo(SCMDescriptor)} is {@code true} within this method.
     *
     * @param builder the builder (invariant: {@link SCMSourceTraitDescriptor#isApplicableTo(SCMDescriptor)} is
     *                {@code true})
     * @param <B>     generic type parameter to ensure type information available.
     * @param <S>     generic type parameter to ensure type information available.
     */
    protected <B extends SCMBuilder<B, S>, S extends SCM> void decorateBuilder(B builder) {
    }

    /**
     * Checks if the supplied category is required by this trait.
     *
     * @param category the category.
     * @return {@code true} if this trait requires the supplied category.
     */
    public final boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        return includeCategory(category);
    }

    /**
     * SPI: Override this method to control whether specific {@link SCMHeadCategory} instances are required.
     *
     * @param category the category.
     * @return {@code true} to require the category.
     */
    protected boolean includeCategory(@NonNull SCMHeadCategory category) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public SCMSourceTraitDescriptor getDescriptor() {
        return (SCMSourceTraitDescriptor) super.getDescriptor();
    }

}
