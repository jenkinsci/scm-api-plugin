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
import hudson.scm.SCM;
import java.util.Arrays;
import java.util.Collection;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

/**
 * Builder for a {@link SCM} instance. Typically instantiated in {@link SCMSource#build(SCMHead, SCMRevision)} or
 * {@link SCMSource#build(SCMHead)} and then decorated by {@link SCMSourceTrait#applyToBuilder(SCMBuilder)} before
 * calling {@link #build()} to generate the return value. Conventions:
 * <ul>
 * <li>The builder is not designed to be shared by multiple threads.</li>
 * <li>All methods should be either {@code final} or {@code abstract} unless there is a documented reason for
 * allowing overrides</li>
 * <li>All "setter" methods will return {@link B} and be called "withXxx"</li>
 * <li>All "getter" methods will be called "xxx()". Callers should not assume that the returned value is resistant
 * from concurrent changes. Implementations should ensure that the returned value is immutable by the caller.
 * In other words, it is intentional for implementations to reduce intermediate allocations by
 * {@code return Collections.unmodifiableList(theList);} rather than the concurrency safe
 * {@code return Collections.unmodifiableList(new ArrayList<>(theList));}
 * </li>
 * </ul>
 *
 * @param <B> the type of {@link SCMBuilder} so that subclasses can chain correctly in their {@link #withHead(SCMHead)}
 *            etc methods.
 * @param <S> the type of {@link SCM} returned by {@link #build()}
 */
public abstract class SCMBuilder<B extends SCMBuilder<B,S>,S extends SCM> {

    /**
     * The base class of {@link SCM} that will be produced by the {@link SCMBuilder}.
     */
    @NonNull
    private final Class<S> clazz;
    /**
     * The {@link SCMHead} to produce the {@link SCM} for.
     */
    @NonNull
    private SCMHead head;
    /**
     * The {@link SCMRevision} to produce the {@link SCM} for or {@code null} to produce the {@link SCM} for the head
     * revision.
     */
    @CheckForNull
    private SCMRevision revision;

    /**
     * Constructor.
     *
     * @param clazz    The base class of {@link SCM} that will be produced by the {@link SCMBuilder}.
     * @param head     The {@link SCMHead} to produce the {@link SCM} for.
     * @param revision The {@link SCMRevision} to produce the {@link SCM} for or {@code null} to produce the
     *                 {@link SCM} for the head revision.
     */
    public SCMBuilder(Class<S> clazz, @NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        this.clazz = clazz;
        this.head = head;
        this.revision = revision;
    }

    /**
     * Returns the {@link SCMHead} to produce the {@link SCM} for.
     *
     * @return the {@link SCMHead} to produce the {@link SCM} for.
     */
    @NonNull
    public final SCMHead head() {
        return head;
    }

    /**
     * Returns the {@link SCMRevision} to produce the {@link SCM} for or {@code null} to produce the {@link SCM} for
     * the head revision.
     *
     * @return the {@link SCMRevision} to produce the {@link SCM} for or {@code null} to produce the {@link SCM} for
     * the head revision.
     */
    @CheckForNull
    public final SCMRevision revision() {
        return revision;
    }

    /**
     * Returns the base class of {@link SCM} that will be produced by the {@link SCMBuilder}.
     *
     * @return the base class of {@link SCM} that will be produced by the {@link SCMBuilder}.
     */
    @NonNull
    public final Class<S> scmClass() {
        return clazz;
    }

    /**
     * Replace the {@link #head()} with a new {@link SCMHead}.
     *
     * @param head the {@link SCMHead} to produce the {@link SCM} for.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final B withHead(@NonNull SCMHead head) {
        this.head = head;
        return (B) this;
    }

    /**
     * Replace the {@link #revision()}  with a new {@link SCMRevision}
     *
     * @param revision the {@link SCMRevision} to produce the {@link SCM} for or {@code null} to produce the
     *                 {@link SCM} for the head revision.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final B withRevision(@CheckForNull SCMRevision revision) {
        this.revision = revision;
        return (B) this;
    }

    /**
     * Apply the {@link SCMSourceTrait} to this {@link SCMBuilder}.
     * @param trait the {@link SCMSourceTrait}.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final B withTrait(@NonNull SCMSourceTrait trait) {
        trait.applyToBuilder(this);
        return (B) this;
    }

    /**
     * Apply the {@link SCMSourceTrait} instances to this {@link SCMBuilder}.
     *
     * @param traits the {@link SCMSourceTrait} instances.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final B withTraits(@NonNull SCMSourceTrait... traits) {
        return withTraits(Arrays.asList(traits));
    }

    /**
     * Apply the {@link SCMSourceTrait} instances to this {@link SCMBuilder}.
     *
     * @param traits the {@link SCMSourceTrait} instances.
     * @return {@code this} for method chaining.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public final B withTraits(@NonNull Collection<SCMSourceTrait> traits) {
        for (SCMSourceTrait trait : traits) {
            withTrait(trait);
        }
        return (B) this;
    }

    /**
     * Instantiates the {@link SCM}.
     * @return the {@link S} instance
     */
    @NonNull
    public abstract S build();
}
