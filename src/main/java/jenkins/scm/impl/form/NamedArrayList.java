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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link ArrayList} that also has an associated name for use with the {@code FormTagLib.traits()} tag.
 *
 * @param <E> the type of element.
 */
@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS",
        justification = "On purpose ignoring 'name' field, not changing current behavior present since ~2 years")
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
        select(source, name, selector, removeSelectedFromSource, destination, Integer.MAX_VALUE);
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
     * @param index                    index at which the specified selection is to be inserted (will be coerced into
     *                                 the valid range
     *                                 to remove the risk of {@link IndexOutOfBoundsException}).
     * @param <E>                      the type of element.
     */
    @SuppressWarnings("unchecked")
    public static <E> void select(@NonNull List<? extends E> source, @NonNull String name,
                                  @CheckForNull Predicate<? super E> selector, boolean removeSelectedFromSource,
                                  @NonNull List<NamedArrayList<? extends E>> destination, int index) {
        NamedArrayList<E> selection = new NamedArrayList<>(name);
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
            destination.add(Math.min(destination.size(), Math.max(index, 0)), selection);
        }
    }

    /**
     * Combines {@link Predicate} instances using a boolean short-circuit logical AND.
     *
     * @param predicates the predicates to combine.
     * @param <E>        type.
     * @return a composite predicate.
     */
    public static <E> Predicate<E> allOf(final Predicate<? super E>... predicates) {
        if (predicates.length < 2) {
            throw new IllegalArgumentException("Must supply at least two predicates");
        }
        return new Predicate<>() {
            @Override
            public boolean test(E e) {
                for (Predicate<? super E> p : predicates) {
                    if (!p.test(e)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Combines {@link Predicate} instances using a boolean short-circuit logical OR.
     *
     * @param predicates the predicates to combine.
     * @param <E>        type.
     * @return a composite predicate.
     */
    public static <E> Predicate<E> anyOf(final Predicate<E>... predicates) {
        if (predicates.length < 2) {
            throw new IllegalArgumentException("Must supply at least two predicates");
        }
        return new Predicate<>() {
            @Override
            public boolean test(E e) {
                for (Predicate<? super E> p : predicates) {
                    if (p.test(e)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a {@link Predicate} that checks if the object class has been annotated with the supplied
     * annotation.
     *
     * @param annotation the annotation to check.
     * @param <A>        the type of annotation.
     * @return the predicate.
     */
    public static <A extends Annotation> Predicate<Object> withAnnotation(final Class<A> annotation) {
        return new Predicate<>() {
            @Override
            public boolean test(Object o) {
                return o.getClass().getAnnotation(annotation) != null;
            }
        };
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
