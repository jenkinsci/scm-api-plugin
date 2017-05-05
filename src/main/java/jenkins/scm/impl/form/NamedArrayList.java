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

package jenkins.scm.impl.form;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import jenkins.scm.api.FormTagLib;

/**
 * An {@link ArrayList} that also has an associated name for use with the {@link FormTagLib#traits()} tag.
 *
 * @param <E> the type of element.
 */
public class NamedArrayList<E> extends ArrayList<E> {
    /**
     * The associated name.
     */
    @NonNull
    private final String name;

    /**
     * Copy constructor.
     *
     * @param name the name.
     * @param c    the collection whose elements are to be placed into this list
     */
    public NamedArrayList(@NonNull String name, Collection<? extends E> c) {
        super(c);
        this.name = name;
    }

    /**
     * Copy constructor.
     *
     * @param name     the name.
     * @param elements the initial elements.
     */
    public NamedArrayList(@NonNull String name, E... elements) {
        super(Arrays.asList(elements));
        this.name = name;
    }

    /**
     * Gets the associate name.
     *
     * @return the associate name.
     */
    @NonNull
    public final String getName() {
        return name;
    }

    /**
     * Helper method that creates a new {@link NamedArrayList} by selecting matching elements from a source list
     * and appends the new {@link NamedArrayList} to a list of {@link NamedArrayList}.
     *
     * @param source                   the list of candidate elements.
     * @param name                     the name.
     * @param selector                 the (optional) selection criteria (if {@code null} then all candidate elements
     *                                 will be added)
     * @param removeSelectedFromSource if {@code true} then the matching elements will be removed from the source.
     * @param destination              the {@link List} of {@link NamedArrayList} to add to (empty selections will
     *                                 not be added)
     * @param <E>                      the type of element.
     */
    @SuppressWarnings("unchecked")
    public static <E> void select(@NonNull List<? extends E> source, @NonNull String name,
                                  @CheckForNull Predicate<? super E> selector, boolean removeSelectedFromSource,
                                  @NonNull List<NamedArrayList<? extends E>> destination) {
        NamedArrayList<E> selection = new NamedArrayList<E>(name);
        if (selector == null) {
            selection.addAll(source);
            if (removeSelectedFromSource) {
                source.clear();
            }
        } else {
            for (Iterator<? extends E> iterator = source.iterator(); iterator.hasNext(); ) {
                E candidate = iterator.next();
                if (selector.test(candidate)) {
                    selection.add(candidate);
                    if (removeSelectedFromSource) {
                        iterator.remove();
                    }
                }
            }
        }
        if (!selection.isEmpty()) {
            destination.add(selection);
        }
    }

    /**
     * Represents a predicate (boolean-valued function) of one argument.
     *
     * @param <T> the type of the input to the predicate
     */
    // TODO replace with java.util.function.Predicate once baseline is Java 8
    //@FunctionalInterface
    //@Restricted(NoExternalUse.class)
    //@RestrictedSince("...")
    //@Deprecated
    public interface Predicate<T> { // extends java.util.function.Predicate<T>
        boolean test(T t);
    }
}
