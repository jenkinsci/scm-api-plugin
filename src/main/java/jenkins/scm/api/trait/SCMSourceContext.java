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
import hudson.model.TaskListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;

/**
 * Represents the context within which a {@link SCMSource} is processing requests. In general this is used as a builder
 * for {@link SCMSourceRequest} instances through {@link SCMSourceContext#newRequest(SCMSource, TaskListener)} but there
 * are some cases (such as {@link SCMHeadEvent} processing) where only the context is required and as such this
 * type will be instantiated to obtain the context but no {@link SCMSourceRequest} will be created. Conventions:
 * <ul>
 * <li>The context is not designed to be shared by multiple threads.</li>
 * <li>All methods should be either {@code final} or {@code abstract} unless there is a documented reason for
 * allowing overrides</li>
 * <li>All "setter" methods will return {@link C} and be called "withXxx"</li>
 * <li>All "getter" methods will called "xxx()" callers should not assume that the returned value is resistent
 * from concurrent changes but implementations should ensure that the returned value is immutable by the caller.
 * In other words, it is intentional to reduce intermediate allocations by
 * {@code return Collections.unmodifiableList(theList);} rather than the concurrency safe
 * {@code return Collections.unmodifiableList(new ArrayList<>(theList));}
 * </li>
 * </ul>
 *
 * @param <C> the type of {@link SCMSourceContext}
 * @param <R> the type of {@link SCMSourceRequest} produced by this context.
 * @since 2.2.0
 */
public abstract class SCMSourceContext<C extends SCMSourceContext<C, R>, R extends SCMSourceRequest> {
    /**
     * The criteria.
     */
    @NonNull
    private final List<SCMSourceCriteria> criteria = new ArrayList<SCMSourceCriteria>();
    /**
     * The pre-filters (i.e. filters that are independent of the {@link SCMSourceRequest}).
     */
    @NonNull
    private final List<SCMHeadPrefilter> prefilters = new ArrayList<SCMHeadPrefilter>();
    /**
     * The {@link SCMSourceRequest} dependent filters.
     */
    @NonNull
    private final List<SCMHeadFilter> filters = new ArrayList<SCMHeadFilter>();
    /**
     * The authorities.
     */
    @NonNull
    private final List<SCMHeadAuthority<?, ?, ?>> authorities = new ArrayList<SCMHeadAuthority<?, ?, ?>>();
    /**
     * The observer.
     */
    @NonNull
    private SCMHeadObserver observer;

    /**
     * Constructor.
     *
     * @param criteria (optional) criteria.
     * @param observer the {@link SCMHeadObserver}.
     */
    public SCMSourceContext(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer) {
        withCriteria(criteria);
        this.observer = observer;
    }

    /**
     * Returns the (possibly empty) list of {@link SCMHeadAuthority} instances that can define trust.
     *
     * @return the (possibly empty) list of {@link SCMHeadAuthority} instances that can define trust.
     */
    @NonNull
    public final List<SCMHeadAuthority<?, ?, ?>> authorities() {
        return Collections.unmodifiableList(authorities);
    }

    /**
     * Returns the (possibly empty) list of criteria that must be met by a candidate {@link SCMHead}.
     *
     * @return the (possibly empty) list of criteria that must be met by a candidate {@link SCMHead}.
     */
    @NonNull
    public final List<SCMSourceCriteria> criteria() {
        return Collections.unmodifiableList(criteria);
    }

    /**
     * Returns the (possibly empty) list of {@link SCMSourceRequest} dependent filters.
     *
     * @return the (possibly empty) list of {@link SCMSourceRequest} dependent filters.
     */
    @NonNull
    public final List<SCMHeadFilter> filters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * Returns the (possibly empty) list of {@link SCMSourceRequest} independent pre-filters.
     *
     * @return the (possibly empty) list of {@link SCMSourceRequest} independent pre-filters.
     */
    @NonNull
    public final List<SCMHeadPrefilter> prefilters() {
        return Collections.unmodifiableList(prefilters);
    }

    /**
     * Returns the {@link SCMHeadObserver}.
     *
     * @return the {@link SCMHeadObserver}.
     */
    @NonNull
    public final SCMHeadObserver observer() {
        return observer;
    }

    /**
     * Adds an additional {@link SCMHeadAuthority}.
     *
     * @param authority the {@link SCMHeadAuthority}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withAuthority(@CheckForNull SCMHeadAuthority authority) {
        if (authority != null) {
            this.authorities.add(authority);
        }
        return (C) this;
    }

    /**
     * Adds an additional {@link SCMSourceCriteria}.
     *
     * @param criteria the {@link SCMSourceCriteria}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withCriteria(@CheckForNull SCMSourceCriteria criteria) {
        if (criteria != null) {
            this.criteria.add(criteria);
        }
        return (C) this;
    }

    /**
     * Adds an additional {@link SCMHeadFilter}.
     *
     * @param filter the {@link SCMHeadFilter}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withFilter(@CheckForNull SCMHeadFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return (C) this;
    }

    /**
     * Adds an additional {@link SCMHeadPrefilter}.
     *
     * @param prefilter the {@link SCMHeadPrefilter}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withPrefilter(@CheckForNull SCMHeadPrefilter prefilter) {
        if (prefilter != null) {
            this.prefilters.add(prefilter);
        }
        return (C) this;
    }

    /**
     * Adds an additional {@link SCMSourceTrait}.
     *
     * @param trait the {@link SCMSourceTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTrait(@NonNull SCMSourceTrait trait) {
        observer = trait.applyToObserver(observer);
        trait.applyToContext(this);
        return (C) this;
    }

    /**
     * Adds additional {@link SCMSourceTrait}s.
     *
     * @param traits the {@link SCMSourceTrait}s.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final C withTraits(@NonNull SCMSourceTrait... traits) {
        return withTraits(Arrays.asList(traits));
    }

    /**
     * Adds additional {@link SCMSourceTrait}s.
     *
     * @param traits the {@link SCMSourceTrait}s.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final C withTraits(@NonNull Collection<SCMSourceTrait> traits) {
        for (SCMSourceTrait trait : traits) {
            withTrait(trait);
        }
        return (C) this;
    }

    /**
     * Creates a new {@link SCMSourceRequest}.
     *
     * @param source   the {@link SCMSource}.
     * @param listener the (optional) {@Link TaskListener}.
     * @return the {@link R}
     */
    @NonNull
    public abstract R newRequest(@NonNull SCMSource source, @CheckForNull TaskListener listener);
}
