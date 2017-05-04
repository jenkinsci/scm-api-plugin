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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;

/**
 * Builder for a {@link SCMSource} instance. Typically instantiated in
 * {@link SCMNavigator#visitSources(SCMSourceObserver)} and then decorated by
 * {@link SCMNavigatorTrait#applyToBuilder(SCMSourceBuilder)} before calling {@link #build()} to generate the return
 * value. Conventions:
 * <ul>
 * <li>The builder is not designed to be shared by multiple threads.</li>
 * <li>All methods should be either {@code final} or {@code abstract} unless there is a documented reason for
 * allowing overrides</li>
 * <li>All "setter" methods will return {@link B} and be called "withXxx"</li>
 * <li>All "getter" methods will called "xxx()" callers should not assume that the returned value is resistent
 * from concurrent changes but implementations should ensure that the returned value is immutable by the caller.
 * In other words, it is intentional to reduce intermediate allocations by
 * {@code return Collections.unmodifiableList(theList);} rather than the concurrency safe
 * {@code return Collections.unmodifiableList(new ArrayList<>(theList));}
 * </li>
 * </ul>
 *
 * @param <B> the type of {@link SCMSourceBuilder} so that subclasses can chain correctly in their
 *            {@link #withTrait(SCMSourceTrait)}  etc methods.
 * @param <S> the type of {@link SCMSource} returned by {@link #build()}. In general this should be a type that
 *            has {@link SCMSourceTrait} support.
 */
public abstract class SCMSourceBuilder<B extends SCMSourceBuilder<B, S>, S extends SCMSource> {

    /**
     * The base class of {@link SCMSource} that will be produced by the {@link SCMSourceBuilder}.
     */
    @NonNull
    private final Class<S> clazz;
    /**
     * The project name.
     */
    @NonNull
    private final String projectName;
    /**
     * The {@link SCMSourceTrait} instances to provide to the {@link SCMSource} (assuming the {@link SCMSource} is
     * {@link SCMSourceTrait} aware.
     */
    @NonNull
    private final List<SCMSourceTrait> traits = new ArrayList<SCMSourceTrait>();

    /**
     * Constructor.
     * @param clazz the base class of {@link SCMSource} that will be produced by the {@link SCMSourceBuilder}.
     * @param projectName the project name.
     */
    public SCMSourceBuilder(@NonNull Class<S> clazz, @NonNull String projectName) {
        this.clazz = clazz;
        this.projectName = projectName;
    }

    /**
     * Returns the base class of {@link SCMSource} that will be produced by the {@link SCMSourceBuilder}.
     * @return the base class of {@link SCMSource} that will be produced by the {@link SCMSourceBuilder}.
     */
    @NonNull
    public final Class<S> sourceClass() {
        return clazz;
    }

    /**
     * Returns the project name.
     * @return the project name.
     */
    @NonNull
    public final String projectName() {
        return projectName;
    }

    /**
     * Returns the {@link SCMSourceTrait} instances to provide to the {@link SCMSource} (assuming the {@link SCMSource}
     * is {@link SCMSourceTrait} aware.
     * @return the {@link SCMSourceTrait} instances to provide to the {@link SCMSource} (assuming the
     * {@link SCMSource} is {@link SCMSourceTrait} aware.
     */
    @NonNull
    public final List<SCMSourceTrait> traits() {
        return Collections.unmodifiableList(traits);
    }


    /**
     * Apply the {@link SCMNavigatorTrait} to this {@link SCMSourceBuilder}.
     *
     * @param trait the {@link SCMNavigatorTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final B withTrait(@NonNull SCMNavigatorTrait trait) {
        trait.applyToBuilder(this);
        return (B) this;
    }

    /**
     * Apply the {@link SCMSourceTrait} to this {@link SCMSourceBuilder} (that is add to {@link #traits()}).
     *
     * @param trait the {@link SCMSourceTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final B withTrait(@NonNull SCMSourceTrait trait) {
        traits.add(trait);
        return (B) this;
    }

    /**
     * Apply the {@link SCMTrait} instances to this {@link SCMSourceBuilder}.
     *
     * @param traits the {@link SCMTrait} instances.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final B withTraits(@NonNull SCMTrait<? extends SCMTrait<?>>... traits) {
        return withTraits(Arrays.asList(traits));
    }

    /**
     * Apply the {@link SCMTrait} instances to this {@link SCMSourceBuilder}.
     *
     * @param traits the {@link SCMTrait} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public B withTraits(@NonNull Collection<? extends SCMTrait<?>> traits) {
        for (SCMTrait<?> trait : traits) {
            if (trait instanceof SCMNavigatorTrait) {
                withTrait((SCMNavigatorTrait)trait);
            } else if (trait instanceof SCMSourceTrait) {
                withTrait((SCMSourceTrait) trait);
            } else {
                throw new IllegalArgumentException("Unsupported trait: " + trait.getClass().getName());
            }
        }
        return (B) this;
    }


    /**
     * Apply the {@link SCMNavigatorRequest} to this {@link SCMSourceBuilder}.
     *
     * @param request the {@link SCMNavigatorRequest} instance.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    public B withRequest(@NonNull SCMNavigatorRequest request) {
        withTraits(request.traits());
        for (SCMSourceDecorator<?,?> decorator: request.decorators()) {
            decorator.applyTo(this, projectName());
        }
        return (B) this;
    }

    /**
     * Instantiates the {@link SCMSource}.
     *
     * @return the {@link S} instance
     */
    @NonNull
    public abstract S build();
}
