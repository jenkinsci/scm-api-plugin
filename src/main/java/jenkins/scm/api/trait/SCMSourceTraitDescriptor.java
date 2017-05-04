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
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

/**
 * Abstract base class for {@link Descriptor} of {@link SCMSourceTrait} implementations.
 *
 * @since 2.2.0
 */
public abstract class SCMSourceTraitDescriptor extends SCMTraitDescriptor<SCMSourceTrait> {

    /**
     * Constructor to use when type inferrence using {@link #SCMSourceTraitDescriptor()} does not work.
     *
     * @param clazz Pass in the type of {@link SCMTrait}
     */
    protected SCMSourceTraitDescriptor(@NonNull Class<? extends SCMSourceTrait> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link SCMSourceTrait} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     */
    protected SCMSourceTraitDescriptor() {
        super();
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified type of {@link SCMBuilder}.
     *
     * @param builderClass the type of {@link SCMBuilder}.
     * @return {@code true} if applicable to the specified type of {@link SCMBuilder}.
     */
    public boolean isApplicableToBuilder(@NonNull Class<? extends SCMBuilder> builderClass) {
        return true;
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMBuilder}.
     *
     * @param builder the {@link SCMBuilder}.
     * @return {@code true} if applicable to the specified type of {@link SCMBuilder}.
     */
    public boolean isApplicableToBuilder(@NonNull SCMBuilder<?, ?> builder) {
        return isApplicableToBuilder(builder.getClass()) && isApplicableToSCM(builder.scmClass());
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified type of {@link SCMSourceContext}.
     *
     * @param contextClass the type of {@link SCMSourceContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceContext}.
     */
    public boolean isApplicableToContext(@NonNull Class<? extends SCMSourceContext> contextClass) {
        return true;
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMSourceContext}.
     *
     * @param context the {@link SCMSourceContext}.
     * @return {@code true} if applicable to the specified type of {@link SCMSourceContext}.
     */
    public boolean isApplicableToContext(@NonNull SCMSourceContext context) {
        return isApplicableToContext(context.getClass());
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMSource}.
     *
     * @param source the {@link SCMSource}.
     * @return {@code true} if applicable to the specified {@link SCMSource}.
     */
    public boolean isApplicableTo(@NonNull SCMSource source) {
        return isApplicableTo(source.getDescriptor());
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified {@link SCMSourceDescriptor}.
     *
     * @param descriptor the {@link SCMSourceDescriptor}.
     * @return {@code true} if applicable to the specified {@link SCMSourceDescriptor}.
     */
    public boolean isApplicableTo(@NonNull SCMSourceDescriptor descriptor) {
        return isApplicableTo(descriptor.getT());
    }

    /**
     * Checks if the {@link SCMSourceTrait} is relevant to the specified type of {@link SCMSource}.
     *
     * @param sourceClass the type of {@link SCMSource}.
     * @return {@code true} if applicable to the specified type of {@link SCMSource}.
     */
    public boolean isApplicableTo(@NonNull Class<? extends SCMSource> sourceClass) {
        return true;
    }

}
