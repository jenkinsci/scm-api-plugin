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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Descriptor;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

/**
 * Abstract base class for {@link Descriptor} of {@link SCMSourceTrait} implementations.
 *
 * @since 2.2.0
 */
public abstract class SCMNavigatorTraitDescriptor extends SCMTraitDescriptor<SCMNavigatorTrait> {

    /**
     * Constructor to use when type inferrence using {@link #SCMNavigatorTraitDescriptor()} does not work.
     *
     * @param clazz Pass in the type of {@link SCMNavigatorTrait}
     */
    protected SCMNavigatorTraitDescriptor(@NonNull Class<? extends SCMNavigatorTrait> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link SCMNavigatorTrait} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     */
    protected SCMNavigatorTraitDescriptor() {
        super();
    }

    /**
     * Returns the type of {@link SCMSourceBuilder} that this {@link SCMNavigatorTrait} is applicable to.
     *
     * @return the type of {@link SCMSourceBuilder} that this {@link SCMNavigatorTrait} is applicable to.
     */
    public Class<? extends SCMSourceBuilder> getBuilderClass() {
        return SCMSourceBuilder.class;
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified type of {@link SCMSourceBuilder}.
     *
     * @param builderClass the type of {@link SCMBuilder}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceBuilder}.
     */
    public boolean isApplicableToBuilder(@NonNull Class<? extends SCMSourceBuilder> builderClass) {
        return getBuilderClass().isAssignableFrom(builderClass);
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMSourceBuilder}.
     *
     * @param builder the {@link SCMSourceBuilder}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceBuilder}.
     */
    public boolean isApplicableToBuilder(@NonNull SCMSourceBuilder<?, ?> builder) {
        return isApplicableToBuilder(builder.getClass()) && isApplicableToSource(builder.sourceClass());
    }

    /**
     * Returns the type of {@link SCMNavigatorContext} that this {@link SCMNavigatorTrait} is applicable to.
     *
     * @return the type of {@link SCMNavigatorContext} that this {@link SCMNavigatorTrait} is applicable to.
     */
    public Class<? extends SCMNavigatorContext> getContextClass() {
        return SCMNavigatorContext.class;
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified type of {@link SCMNavigatorContext}.
     *
     * @param contextClass the type of {@link SCMSourceContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceContext}.
     */
    public boolean isApplicableToContext(@NonNull Class<? extends SCMNavigatorContext> contextClass) {
        return getContextClass().isAssignableFrom(contextClass);
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified {@link SCMNavigatorContext}.
     *
     * @param context the {@link SCMNavigatorContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMNavigatorContext}.
     */
    public boolean isApplicableToContext(@NonNull SCMNavigatorContext context) {
        return isApplicableToContext(context.getClass());
    }

    /**
     * Returns the type of {@link SCMSource} that this {@link SCMNavigatorTrait} is applicable to.
     *
     * @return the type of {@link SCMSource} that this {@link SCMNavigatorTrait} is applicable to.
     */
    public Class<? extends SCMSource> getSourceClass() {
        return SCMSource.class;
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified type of {@link SCMSource}.
     *
     * @param sourceClass the type of {@link SCMSource}.
     * @return {@code true} if applicable to the specified type of {@link SCMSource}.
     */
    public boolean isApplicableToSource(@NonNull Class<? extends SCMSource> sourceClass) {
        return getSourceClass().isAssignableFrom(sourceClass);
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified {@link SCMSourceDescriptor}.
     *
     * @param descriptor the {@link SCMSourceDescriptor}.
     * @return {@code true} if applicable to the specified {@link SCMSourceDescriptor}.
     */
    public boolean isApplicableToSource(@NonNull SCMSourceDescriptor descriptor) {
        return isApplicableToSource(descriptor.getT());
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified {@link SCMSource}.
     *
     * @param source the {@link SCMSource}.
     * @return {@code true} if applicable to the specified {@link SCMSource}.
     */
    public boolean isApplicableToSource(@NonNull SCMSource source) {
        return isApplicableToSource(source.getDescriptor());
    }

    /**
     * Returns the type of {@link SCMNavigator} that this {@link SCMNavigatorTrait} is applicable to.
     *
     * @return the type of {@link SCMNavigator} that this {@link SCMNavigatorTrait} is applicable to.
     */
    public Class<? extends SCMNavigator> getNavigatorClass() {
        return SCMNavigator.class;
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified type of {@link SCMNavigator}.
     *
     * @param navigatorClass the type of {@link SCMNavigator}.
     * @return {@code true} if applicable to the specified type of {@link SCMNavigator}.
     */
    public boolean isApplicableTo(@NonNull Class<? extends SCMNavigator> navigatorClass) {
        return getNavigatorClass().isAssignableFrom(navigatorClass);
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified {@link SCMNavigatorDescriptor}.
     *
     * @param descriptor the {@link SCMNavigatorDescriptor}.
     * @return {@code true} if applicable to the specified {@link SCMNavigatorDescriptor}.
     */
    public boolean isApplicableTo(@NonNull SCMNavigatorDescriptor descriptor) {
        return isApplicableTo(descriptor.getT());
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified {@link SCMNavigator}.
     *
     * @param navigator the {@link SCMNavigator}.
     * @return {@code true} if applicable to the specified {@link SCMNavigator}.
     */
    public boolean isApplicableTo(@NonNull SCMNavigator navigator) {
        return isApplicableTo(navigator.getDescriptor());
    }

}
