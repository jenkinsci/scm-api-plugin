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

    protected SCMNavigatorTraitDescriptor(Class<? extends SCMNavigatorTrait> clazz) {
        super(clazz);
    }

    protected SCMNavigatorTraitDescriptor() {
        super();
    }

    /**
     * Checks if the {@link SCMNavigatorTrait} is relevant to the specified type of {@link SCMSourceBuilder}.
     *
     * @param builderClass the type of {@link SCMBuilder}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceBuilder}.
     */
    public boolean isApplicableToBuilder(@NonNull Class<? extends SCMSourceBuilder> builderClass) {
        return true;
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
     * Checks if the {@link SCMSourceTrait} is relevant to the specified type of {@link SCMNavigatorContext}.
     *
     * @param contextClass the type of {@link SCMSourceContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceContext}.
     */
    public boolean isApplicableToContext(@NonNull Class<? extends SCMNavigatorContext> contextClass) {
        return true;
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMNavigatorContext}.
     *
     * @param context the {@link SCMNavigatorContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMNavigatorContext}.
     */
    public boolean isApplicableToContext(@NonNull SCMNavigatorContext context) {
        return isApplicableToContext(context.getClass());
    }

    public boolean isApplicableToSource(SCMSource source) {
        return isApplicableToSource(source.getDescriptor());
    }

    public boolean isApplicableToSource(SCMSourceDescriptor descriptor) {
        return isApplicableToSource(descriptor.getT());
    }

    public boolean isApplicableToSource(Class<? extends SCMSource> sourceClass) {
        return true;
    }

    public boolean isApplicableTo(SCMNavigator navigator) {
        return isApplicableTo(navigator.getDescriptor());
    }

    public boolean isApplicableTo(SCMNavigatorDescriptor descriptor) {
        return true;
    }

}
