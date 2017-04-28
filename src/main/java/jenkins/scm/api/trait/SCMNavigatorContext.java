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
 *
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
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceObserver;

/**
 * Represents the context within which a {@link SCMNavigator} is processing requests. In general this is used as a
 * builder for {@link SCMNavigatorRequest} instances through
 * {@link SCMNavigatorContext#newRequest(SCMNavigator, SCMSourceObserver)} but there are some cases (such as
 * {@link SCMSourceEvent} processing) where only the context is required and as such this
 * type will be instantiated to obtain the context but no {@link SCMNavigatorRequest} will be created.
 *
 * @param <C> the type of {@link SCMNavigatorContext}
 * @param <R> the type of {@link SCMNavigatorRequest} produced by this context.
 * @since 2.2.0
 */
public abstract class SCMNavigatorContext<C extends SCMNavigatorContext<C, R>, R extends SCMNavigatorRequest> {
    @NonNull
    private final List<SCMSourcePrefilter> prefilters = new ArrayList<SCMSourcePrefilter>();
    @NonNull
    private final List<SCMSourceFilter> filters = new ArrayList<SCMSourceFilter>();
    @NonNull
    private final List<SCMSourceTrait> traits = new ArrayList<SCMSourceTrait>();
    @NonNull
    private final List<SCMSourceDecorator<?,?>> decorators = new ArrayList<SCMSourceDecorator<?, ?>>();

    public SCMNavigatorContext() { }

    @SuppressWarnings("unchecked")
    public C withFilter(@CheckForNull SCMSourceFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C withPrefilter(@CheckForNull SCMSourcePrefilter prefilter) {
        if (prefilter != null) {
            this.prefilters.add(prefilter);
        }
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C withPrefilters(@CheckForNull Collection<SCMSourcePrefilter> prefilters) {
        if (prefilters != null) {
            this.prefilters.addAll(prefilters);
        }
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C withTrait(@NonNull SCMNavigatorTrait trait) {
        trait.applyToContext(this);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C withTrait(@NonNull SCMSourceTrait trait) {
        traits.add(trait);
        return (C) this;
    }

    public C withTraits(@NonNull SCMTrait<? extends SCMTrait<?>>... traits) {
        return withTraits(Arrays.asList(traits));
    }

    @SuppressWarnings("unchecked")
    public C withTraits(@NonNull Collection<? extends SCMTrait<?>> traits) {
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

    @SuppressWarnings("unchecked")
    public C withDecorator(@NonNull SCMSourceDecorator<?,?> decorator) {
        decorators.add(decorator);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C withDecorators(@NonNull SCMSourceDecorator<?,?>... decorator) {
        return withDecorators(Arrays.asList(decorator));
    }

    @SuppressWarnings("unchecked")
    public C withDecorators(@NonNull Collection<? extends SCMSourceDecorator<?,?>> decorators) {
        this.decorators.addAll(decorators);
        return (C) this;
    }

    @NonNull
    public List<SCMSourceTrait> traits() {
        return new ArrayList<SCMSourceTrait>(traits);
    }

    @NonNull
    public List<SCMSourceDecorator<?,?>> decorators() {
        return new ArrayList<SCMSourceDecorator<?,?>>(decorators);
    }

    @NonNull
    public List<SCMSourceFilter> filters() {
        return Collections.unmodifiableList(filters);
    }

    @NonNull
    public List<SCMSourcePrefilter> prefilters() {
        return Collections.unmodifiableList(prefilters);
    }

    public abstract R newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer);
}
