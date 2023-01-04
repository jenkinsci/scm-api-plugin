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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceObserver;

/**
 * Represents the context within which a {@link SCMNavigator} is processing requests. In general this is used as a
 * builder for {@link SCMNavigatorRequest} instances through
 * {@link SCMNavigatorContext#newRequest(SCMNavigator, SCMSourceObserver)} but there are some cases (such as
 * {@link SCMSourceEvent} processing) where only the context is required and as such this
 * type will be instantiated to obtain the context but no {@link SCMNavigatorRequest} will be created.
 * Conventions:
 * <ul>
 * <li>The context is not designed to be shared by multiple threads.</li>
 * <li>All methods should be either {@code final} or {@code abstract} unless there is a documented reason for
 * allowing overrides</li>
 * <li>All "setter" methods will return {@link C} and be called "withXxx"</li>
 * <li>All "getter" methods will be called "xxx()". Callers should not assume that the returned value is resistant
 * from concurrent changes. Implementations should ensure that the returned value is immutable by the caller.
 * In other words, it is intentional for implementations to reduce intermediate allocations by
 * {@code return Collections.unmodifiableList(theList);} rather than the concurrency safe
 * {@code return Collections.unmodifiableList(new ArrayList<>(theList));}
 * </li>
 * </ul>
 *
 * @param <C> the type of {@link SCMNavigatorContext}
 * @param <R> the type of {@link SCMNavigatorRequest} produced by this context.
 * @since 2.2.0
 */
public abstract class SCMNavigatorContext<C extends SCMNavigatorContext<C, R>, R extends SCMNavigatorRequest> {
    /**
     * The pre-filters that do not need the context of a request.
     */
    @NonNull
    private final List<SCMSourcePrefilter> prefilters = new ArrayList<>();
    /**
     * The filters that need the context of a request.
     */
    @NonNull
    private final List<SCMSourceFilter> filters = new ArrayList<>();
    /**
     * The traits to apply to {@link SCMSource} instances for discovered projects.
     */
    @NonNull
    private final List<SCMSourceTrait> traits = new ArrayList<>();
    /**
     * The decorators to customize a subset of {@link SCMSource} instances.
     */
    @NonNull
    private final List<SCMSourceDecorator<?,?>> decorators = new ArrayList<>();

    /**
     * Constructor.
     */
    public SCMNavigatorContext() { }

    /**
     * Returns the (possibly empty) list of {@link SCMSourceDecorator} instances to apply to discovered projects.
     *
     * @return the (possibly empty) list of {@link SCMSourceDecorator} instances to apply to discovered projects.
     */
    @NonNull
    public final List<SCMSourceDecorator<?, ?>> decorators() {
        return Collections.unmodifiableList(decorators);
    }

    /**
     * Returns the (possibly empty) list of {@link SCMNavigatorRequest} dependent filters.
     *
     * @return the (possibly empty) list of {@link SCMNavigatorRequest} dependent filters.
     */
    @NonNull
    public final List<SCMSourceFilter> filters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * Returns the (possibly empty) list of {@link SCMNavigatorRequest} independent pre-filters.
     *
     * @return the (possibly empty) list of {@link SCMNavigatorRequest} independent pre-filters.
     */
    @NonNull
    public final List<SCMSourcePrefilter> prefilters() {
        return Collections.unmodifiableList(prefilters);
    }

    /**
     * Returns the (possibly empty) list of {@link SCMSourceTrait} instances to apply to discovered projects.
     *
     * @return the (possibly empty) list of {@link SCMSourceTrait} instances to apply to discovered projects.
     */
    @NonNull
    public final List<SCMSourceTrait> traits() {
        return Collections.unmodifiableList(traits);
    }


    /**
     * Adds the supplied {@link SCMSourceFilter}.
     *
     * @param filter the additional {@link SCMSourceFilter}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withFilter(@CheckForNull SCMSourceFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return (C) this;
    }

    /**
     * Adds the supplied {@link SCMSourcePrefilter}.
     *
     * @param prefilter the additional {@link SCMSourcePrefilter}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withPrefilter(@CheckForNull SCMSourcePrefilter prefilter) {
        if (prefilter != null) {
            this.prefilters.add(prefilter);
        }
        return (C) this;
    }

    /**
     * Applies the supplied {@link SCMNavigatorTrait}.
     *
     * @param trait the additional {@link SCMNavigatorTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTrait(@NonNull SCMNavigatorTrait trait) {
        trait.applyToContext(this);
        return (C) this;
    }

    /**
     * Adds the supplied {@link SCMSourceTrait}.
     *
     * @param trait the additional {@link SCMSourceTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTrait(@NonNull SCMSourceTrait trait) {
        traits.add(trait);
        return (C) this;
    }

    /**
     * Adds / applies the supplied {@link SCMTrait}.
     *
     * @param traits the additional {@link SCMTrait} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTraits(@NonNull SCMTrait<? extends SCMTrait<?>>... traits) {
        return withTraits(Arrays.asList(traits));
    }

    /**
     * Adds / applies the supplied {@link SCMTrait}.
     *
     * @param traits the additional {@link SCMTrait} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTraits(@NonNull Collection<? extends SCMTrait<?>> traits) {
        for (SCMTrait<?> trait : traits) {
            if (trait instanceof SCMNavigatorTrait) {
                withTrait((SCMNavigatorTrait) trait);
            } else if (trait instanceof SCMSourceTrait) {
                withTrait((SCMSourceTrait) trait);
            } else {
                throw new IllegalArgumentException("Unsupported trait: " + trait.getClass().getName());
            }
        }
        return (C) this;
    }

    /**
     * Adds the supplied {@link SCMSourceDecorator}.
     *
     * @param decorator the additional {@link SCMSourceDecorator}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withDecorator(@NonNull SCMSourceDecorator<?, ?> decorator) {
        decorators.add(decorator);
        return (C) this;
    }

    /**
     * Adds the supplied {@link SCMSourceDecorator} instances.
     *
     * @param decorators the additional {@link SCMSourceDecorator} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withDecorators(@NonNull SCMSourceDecorator<?, ?>... decorators) {
        return withDecorators(Arrays.asList(decorators));
    }

    /**
     * Adds the supplied {@link SCMSourceDecorator} instances.
     *
     * @param decorators the additional {@link SCMSourceDecorator} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withDecorators(@NonNull Collection<? extends SCMSourceDecorator<?, ?>> decorators) {
        this.decorators.addAll(decorators);
        return (C) this;
    }

    /**
     * Creates a new {@link SCMNavigatorRequest}.
     *
     * @param navigator the {@link SCMNavigator}.
     * @param observer  the {@link SCMSourceObserver}.
     * @return the {@link R}
     */
    @NonNull
    public abstract R newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer);
}
