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

package jenkins.scm.api.trait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import java.util.ArrayList;
import java.util.List;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;

/**
 * Represents a trait of behaviour or configuration that can be applied to a {@link SCMSource}.
 *
 * @since 2.2.0
 */
public class SCMNavigatorTrait extends SCMTrait<SCMNavigatorTrait> {

    /**
     * Applies this trait to the {@link SCMNavigatorContext}.
     *
     * @param context the context.
     */
    public final void applyToContext(SCMNavigatorContext<?, ?> context) {
        if (getDescriptor().isApplicableToContext(context.getClass())) {
            // guard against non-applicable
            decorateContext((SCMNavigatorContext) context);
        }
    }

    /**
     * SPI: Override this method to decorate a {@link SCMNavigatorContext}. You can assume that your
     * {@link SCMNavigatorTraitDescriptor#isApplicableToContext(Class)} is {@code true} within this method.
     *
     * @param context the context (invariant: {@link SCMNavigatorTraitDescriptor#isApplicableToContext(Class)} is {@code true})
     * @param <B>     generic type parameter to ensure type information available.
     * @param <R>     generic type parameter to ensure type information available.
     */
    protected <B extends SCMNavigatorContext<B, R>, R extends SCMNavigatorRequest> void decorateContext(B context) {
    }

    /**
     * Applies this trait to an observer for use during a {@link SCMNavigatorRequest}.
     *
     * @param observer the observer.
     * @return the supplied observer or a wrapped variant of it.
     */
    @NonNull
    public final SCMSourceObserver applyToObserver(@NonNull SCMSourceObserver observer) {
        return decorateObserver(observer);
    }

    /**
     * SPI: Override this method to decorate the {@link SCMSourceObserver} used during a {@link SCMNavigatorRequest}.
     *
     * @param observer the observer.
     * @return the supplied observer or a wrapped variant of it.
     */
    @NonNull
    protected SCMSourceObserver decorateObserver(@NonNull SCMSourceObserver observer) {
        return observer;
    }

    /**
     * Applies this trait to the {@link SCMBuilder}.
     *
     * @param builder the builder.
     */
    public final void applyToBuilder(SCMSourceBuilder<?, ?> builder) {
        if (!getDescriptor().isApplicableToBuilder(builder)) {
            // guard against non-applicable
        }
        decorateBuilder((SCMSourceBuilder) builder);
    }

    /**
     * SPI: Override this method to decorate a {@link SCMBuilder}. You can assume that your
     * {@link SCMSourceTraitDescriptor#isApplicableToBuilder(SCMBuilder)} is {@code true} within this method.
     *
     * @param builder the builder (invariant: {@link SCMSourceTraitDescriptor#isApplicableToBuilder(SCMBuilder)} is
     *                {@code true})
     * @param <B>     generic type parameter to ensure type information available.
     * @param <S>     generic type parameter to ensure type information available.
     */
    protected <B extends SCMSourceBuilder<B, S>, S extends SCMSource> void decorateBuilder(B builder) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public SCMNavigatorTraitDescriptor getDescriptor() {
        return (SCMNavigatorTraitDescriptor) super.getDescriptor();
    }

    /**
     * Returns all the {@link SCMNavigatorTraitDescriptor} instances.
     *
     * @return all the {@link SCMNavigatorTraitDescriptor} instances.
     */
    public static DescriptorExtensionList<SCMNavigatorTrait, SCMNavigatorTraitDescriptor> all() {
        return SCMTrait.all(SCMNavigatorTrait.class);
    }

    /**
     * Returns the subset of {@link SCMNavigatorTraitDescriptor} instances that are applicable to the specified types
     * of {@link SCMNavigatorContext} and {@link SCMSourceBuilder}.
     *
     * @param contextClass (optional) type of {@link SCMNavigatorContext}.
     * @param builderClass (optional) type of {@link SCMSourceBuilder}.
     * @return the list of matching {@link SCMNavigatorTraitDescriptor} instances.
     */
    public static List<SCMNavigatorTraitDescriptor> _for(
            @CheckForNull Class<? extends SCMNavigatorContext> contextClass,
            @CheckForNull Class<? extends SCMSourceBuilder> builderClass) {
        return _for(null, contextClass, builderClass);
    }

    /**
     * Returns the subset of {@link SCMNavigatorTraitDescriptor} instances that are applicable to the specified
     * {@link SCMNavigatorDescriptor} and specified types of {@link SCMNavigatorContext} and {@link SCMSourceBuilder}.
     *
     * @param scmNavigator (optional) {@link SCMNavigatorDescriptor}.
     * @param contextClass (optional) type of {@link SCMNavigatorContext}.
     * @param builderClass (optional) type of {@link SCMSourceBuilder}.
     * @return the list of matching {@link SCMNavigatorTraitDescriptor} instances.
     */
    public static List<SCMNavigatorTraitDescriptor> _for(
            @CheckForNull SCMNavigatorDescriptor scmNavigator,
            @CheckForNull Class<? extends SCMNavigatorContext> contextClass,
            @CheckForNull Class<? extends SCMSourceBuilder> builderClass) {
        List<SCMNavigatorTraitDescriptor> result = new ArrayList<SCMNavigatorTraitDescriptor>();
        if (scmNavigator != null) {
            for (SCMNavigatorTraitDescriptor d : all()) {
                if ((contextClass == null || d.isApplicableToContext(contextClass))
                        && (builderClass == null || d.isApplicableToBuilder(builderClass))
                        && d.isApplicableTo(scmNavigator)) {
                    result.add(d);
                }
            }
        } else {
            for (SCMNavigatorTraitDescriptor d : all()) {
                if ((contextClass == null || d.isApplicableToContext(contextClass))
                        && (builderClass == null || d.isApplicableToBuilder(builderClass))) {
                    result.add(d);
                }
            }
        }
        return result;
    }

}
