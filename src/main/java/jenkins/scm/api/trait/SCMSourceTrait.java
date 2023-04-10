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
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

/**
 * Represents a trait of behaviour or configuration that can be applied to a {@link SCMSource}.
 *
 * @since 2.2.0
 */
public abstract class SCMSourceTrait extends SCMTrait<SCMSourceTrait> {

    /**
     * Applies this trait to the {@link SCMSourceContext}.
     *
     * @param context the context.
     */
    public final void applyToContext(SCMSourceContext<?, ?> context) {
        SCMSourceTraitDescriptor d = getDescriptor();
        if (d.getContextClass().isInstance(context) && d.isApplicableToContext(context.getClass())) {
            // guard against non-applicable
            decorateContext(context);
        }
    }

    /**
     * SPI: Override this method to decorate a {@link SCMSourceContext}. You can assume that your
     * {@link SCMSourceTraitDescriptor#isApplicableToContext(Class)} is {@code true} within this method and that
     * the provided context is an instance of {@link SCMSourceTraitDescriptor#getContextClass()}.
     *
     * @param context the context (invariant: {@link SCMSourceTraitDescriptor#isApplicableToContext(Class)} is {@code
     *                true} and {@link SCMSourceTraitDescriptor#getContextClass()} {@link Class#isInstance(Object)})
     *                is {@code true})
     */
    protected void decorateContext(SCMSourceContext<?, ?> context) {
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
        SCMSourceTraitDescriptor d = getDescriptor();
        if (d.getBuilderClass().isInstance(builder) && d.isApplicableToBuilder(builder)) {
            // guard against non-applicable
            decorateBuilder(builder);
        }
    }

    /**
     * SPI: Override this method to decorate a {@link SCMBuilder}. You can assume that your
     * {@link SCMSourceTraitDescriptor#isApplicableToBuilder(SCMBuilder)} is {@code true} within this method and that
     * the provided builder is an instance of {@link SCMSourceTraitDescriptor#getBuilderClass()}.
     *
     * @param builder the builder (invariant: {@link SCMSourceTraitDescriptor#isApplicableToBuilder(SCMBuilder)} is
     *                {@code true} and {@link SCMSourceTraitDescriptor#getBuilderClass()}
     *                {@link Class#isInstance(Object)}) is {@code true})
     */
    protected void decorateBuilder(SCMBuilder<?, ?> builder) {
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
    public SCMSourceTraitDescriptor getDescriptor() {
        return (SCMSourceTraitDescriptor) super.getDescriptor();
    }

    /**
     * Returns all the {@link SCMSourceTraitDescriptor} instances.
     *
     * @return all the {@link SCMSourceTraitDescriptor} instances.
     */
    public static DescriptorExtensionList<SCMSourceTrait, SCMSourceTraitDescriptor> all() {
        return SCMTrait.all(SCMSourceTrait.class);
    }

    /**
     * Returns the subset of {@link SCMSourceTraitDescriptor} instances that are applicable to the specified types
     * of {@link SCMSourceContext} and {@link SCMSourceBuilder}.
     *
     * @param contextClass (optional) type of {@link SCMSourceContext}.
     * @param builderClass (optional) type of {@link SCMBuilder}.
     * @return the list of matching {@link SCMSourceTraitDescriptor} instances.
     */
    public static List<SCMSourceTraitDescriptor> _for(
            @CheckForNull Class<? extends SCMSourceContext> contextClass,
            @CheckForNull Class<? extends SCMBuilder> builderClass) {
        return _for(null, contextClass, builderClass);
    }

    /**
     * Returns the subset of {@link SCMSourceTraitDescriptor} instances that are applicable to the specified
     * {@link SCMSourceDescriptor} and specified types of {@link SCMNavigatorContext} and {@link SCMSourceBuilder}.
     *
     * @param scmSource    (optional) {@link SCMSourceDescriptor}.
     * @param contextClass (optional) type of {@link SCMSourceContext}.
     * @param builderClass (optional) type of {@link SCMBuilder}.
     * @return the list of matching {@link SCMSourceTraitDescriptor} instances.
     */
    public static List<SCMSourceTraitDescriptor> _for(
            @CheckForNull SCMSourceDescriptor scmSource,
            @CheckForNull Class<? extends SCMSourceContext> contextClass,
            @CheckForNull Class<? extends SCMBuilder> builderClass) {
        List<SCMSourceTraitDescriptor> result = new ArrayList<>();
        if (scmSource != null) {
            for (SCMSourceTraitDescriptor d : all()) {
                if ((contextClass == null || d.isApplicableToContext(contextClass))
                        && (builderClass == null || d.isApplicableToBuilder(builderClass))
                        && d.isApplicableTo(scmSource)) {
                    result.add(d);
                }
            }
        } else {
            for (SCMSourceTraitDescriptor d : all()) {
                if ((contextClass == null || d.isApplicableToContext(contextClass))
                        && (builderClass == null || d.isApplicableToBuilder(builderClass))) {
                    result.add(d);
                }
            }
        }
        return result;
    }

}
