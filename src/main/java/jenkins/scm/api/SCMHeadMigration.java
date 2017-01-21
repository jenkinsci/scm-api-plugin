/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc..
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
package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * If a {@link SCMSource} plugin needs to migrate the implementation classes for {@link SCMHead} this extension
 * point allows the plugin to register type migrations. For speed of migration implementations should just jump directly
 * to the final end-point and not expect recursive chain walking.
 *
 * @since 2.0.2
 */
public abstract class SCMHeadMigration<S extends SCMSource, H extends SCMHead, R extends SCMRevision> implements ExtensionPoint {
    /**
     * The {@link SCMSource} that the migration applies to.
     */
    private final Class<S> sourceClass;
    /**
     * The {@link SCMHead} that the migration applies to.
     */
    private final Class<H> headClass;
    /**
     * The {@link SCMRevision} that the migration applies to.
     */
    private final Class<R> revisionClass;

    /**
     * Constructor.
     * @param sourceClass the {@link SCMSource} that the migration applies to.
     * @param headClass the {@link SCMHead} that the migration applies to.
     * @param revisionClass the {@link SCMRevision} that the migration applies to.
     */
    protected SCMHeadMigration(Class<S> sourceClass, Class<H> headClass, Class<R> revisionClass) {
        this.sourceClass = sourceClass;
        this.headClass = headClass;
        this.revisionClass = revisionClass;
    }

    /**
     * Gets the {@link SCMSource} that the migration applies to.
     *
     * @return the {@link SCMSource} that the migration applies to.
     */
    public final Class<S> getSCMSourceClass() {
        return sourceClass;
    }

    /**
     * Gets the {@link SCMHead} that the migration applies to.
     *
     * @return the {@link SCMHead} that the migration applies to.
     */
    public final Class<H> getSCMHeadClass() {
        return headClass;
    }

    /**
     * Gets the {@link SCMRevision} that the migration applies to.
     *
     * @return the {@link SCMRevision} that the migration applies to.
     */
    public final Class<R> getSCMRevisionClass() {
        return revisionClass;
    }

    /**
     * Perform a migration.
     * <p>
     * <strong>Note:</strong> if you migrate a {@link SCMHead} then most likely you will also want to migrate the
     * {@link SCMRevision} instances associated with that {@link SCMHead} - at the very least to update
     * {@link SCMRevision#getHead()}.
     *
     * @param source the source instance.
     * @param head the candidate head.
     * @return the migrated head or {@code null} if the migration was not appropriate.
     */
    @CheckForNull
    public abstract SCMHead migrate(@NonNull S source, @NonNull H head);

    /**
     * Perform a migration.
     * @param source the source instance.
     * @param revision the candidate revision.
     * @return the migrated revision or {@code null} if the migration was not appropriate.
     */
    @CheckForNull
    public SCMRevision migrate(@NonNull S source, @NonNull R revision) {
        return null;
    }

    /**
     * Perform a migration.
     *
     * @param source the source instance.
     * @param head the candidate head.
     * @return the migrated head or the original head.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static SCMHead readResolveSCMHead(@NonNull SCMSource source, @NonNull SCMHead head) {
        for (SCMHeadMigration m : ExtensionList.lookup(SCMHeadMigration.class)) {
            if (m.sourceClass.isInstance(source)
                    && m.headClass.isInstance(head)) {
                SCMHead migrated = m.migrate(source, head);
                if (migrated != null) {
                    return migrated;
                }
            }
        }
        return head;
    }
    /**
     * Perform a migration.
     *
     * @param source the source instance.
     * @param revision the candidate revision.
     * @return the migrated revision or the original revision.
     */
    @SuppressWarnings("unchecked")
    @CheckForNull
    public static SCMRevision readResolveSCMRevision(@NonNull SCMSource source, @CheckForNull SCMRevision revision) {
        if (revision == null) {
            return null;
        }
        SCMHead head = revision.getHead();
        for (SCMHeadMigration m : ExtensionList.lookup(SCMHeadMigration.class)) {
            if (m.sourceClass.isInstance(source)
                    && m.headClass.isInstance(head)
                    && m.revisionClass.isInstance(revision)) {
                SCMRevision migrated = m.migrate(source, revision);
                if (migrated != null) {
                    return migrated;
                }
            }
        }
        return revision;
    }
}
