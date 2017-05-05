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
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;

/**
 * Abstract base class for {@link Descriptor} of {@link SCMTrait} implementations.
 *
 * @since 2.2.0
 */
public abstract class SCMTraitDescriptor<T extends SCMTrait<T>> extends Descriptor<T> {

    // Constructors are package private in order to restrict trait specializations to the ones defined in this plugin

    /**
     * Constructor to use when type inferrence using {@link #SCMTraitDescriptor()} does not work.
     *
     * @param clazz Pass in the type of {@link SCMTrait}
     */
    /*package*/ SCMTraitDescriptor(@NonNull Class<? extends T> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link SCMTrait} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     */
    /*package*/ SCMTraitDescriptor() {
        super();
    }

    /**
     * Returns the type of {@link SCM} that this {@link SCMTrait} is applicable to.
     *
     * @return the type of {@link SCM} that this {@link SCMTrait} is applicable to.
     */
    public Class<? extends SCM> getScmClass() {
        return SCM.class;
    }

    /**
     * Checks if the {@link SCMTrait} is relevant to the specified type of {@link SCM}.
     *
     * @param scmClass the type of {@link SCM}.
     * @return {@code true} if applicable to the specified type of {@link SCM}.
     */
    public boolean isApplicableToSCM(@NonNull Class<? extends SCM> scmClass) {
        return getScmClass().isAssignableFrom(scmClass);
    }

    /**
     * Checks if the {@link SCMTrait} is relevant to the specified {@link SCM}.
     *
     * @param scm the {@link SCMDescriptor} for the type of {@link SCM}.
     * @return {@code true} if applicable to the specified type of {@link SCM}.
     */
    public boolean isApplicableToSCM(@NonNull SCMDescriptor<?> scm) {
        return isApplicableToSCM(scm.clazz);
    }

}
