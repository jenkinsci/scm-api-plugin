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
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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

    /**
     * Converts the supplied list of {@link SCMTrait} instances into a list where there is at most one instance of
     * each trait.
     *
     * @param list the list to apply the constraint to.
     * @param <T>  type of {@link SCMTrait}.
     * @return a new list that contains the first instance of any type of trait in the supplied list.
     * @since 2.2.0
     */
    @NonNull
    public static <T extends SCMTrait<?>> ArrayList<T> asSetList(@CheckForNull Iterable<? extends T> list) {
        ArrayList<T> result = new ArrayList<T>();
        if (list != null) {
            Set<Class> seen = new HashSet<Class>();
            for (T trait : list) {
                if (trait == null) {
                    continue;
                }
                if (seen.contains(trait.getClass())) {
                    continue;
                }
                seen.add(trait.getClass());
                result.add(trait);
            }
        }
        return result;
    }

    /**
     * Converts the supplied instance and list of {@link SCMTrait} instances into a list where there is at most one
     * instance of each trait.
     *
     * @param first an entry to prepend to the list (will displace any duplicates in the list)
     * @param list the list to apply the constraint to.
     * @param <T>  type of {@link SCMTrait}.
     * @return a new list that contains the first instance of any type of trait in the supplied list.
     * @since 2.2.0
     */
    public static <T extends SCMTrait<?>> ArrayList<T> asSetList(@NonNull T first, @CheckForNull Iterable<? extends T> list) {
        ArrayList<T> result = new ArrayList<T>();
        result.add(first);
        if (list != null) {
            Set<Class> seen = new HashSet<Class>();
            seen.add(first.getClass());
            for (T trait : list) {
                if (trait == null) {
                    continue;
                }
                if (seen.contains(trait.getClass())) {
                    continue;
                }
                seen.add(trait.getClass());
                result.add(trait);
            }
        }
        return result;
    }

    /**
     * Finds the trait of the required type.
     *
     * @param traits the traits to search.
     * @param clazz  the type of trait.
     * @param <T>    the type of trait.
     * @return the matching trait from the supplied traits or {@code null} if there is no matching trait.
     * @since 2.2.0
     */
    @CheckForNull
    public static <T extends SCMTrait<?>> T find(@NonNull Iterable<?> traits, @NonNull Class<T> clazz) {
        for (Object trait : traits) {
            if (clazz.isInstance(trait)) {
                return clazz.cast(trait);
            }
        }
        return null;
    }

}
