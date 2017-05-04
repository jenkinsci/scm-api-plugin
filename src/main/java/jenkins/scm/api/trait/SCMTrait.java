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

import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;

/**
 * Base class for common traits.
 *
 * @param <T> the type of {@link SCMTrait} specialization.
 */
public class SCMTrait<T extends SCMTrait<T>> extends AbstractDescribableImpl<T> {
    /**
     * {@inheritDoc}
     */
    @Override
    public SCMTraitDescriptor<T> getDescriptor() {
        return (SCMTraitDescriptor<T>) super.getDescriptor();
    }

    /**
     * Returns all the {@link SCMTrait} instances of the supplied specialization.
     *
     * @param specialization the specialization of {@link SCMTrait}
     * @return all the {@link SCMTrait} instances of the supplied specialization.
     */
    /*package*/
    static <T extends SCMTrait<T>, D extends SCMTraitDescriptor<T>> DescriptorExtensionList<T, D> all(
            Class<T> specialization) {
        return Jenkins.getActiveInstance().getDescriptorList(specialization);
    }

}
