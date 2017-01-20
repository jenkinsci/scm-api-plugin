package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * If a {@link SCMSource} plugin needs to migrate the implementation classes for {@link SCMHead} this extension
 * point allows the plugin to register type migrations. For speed of migration implementations should just jump direct
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
     *  @param sourceClass The {@link SCMSource} that the migration applies to.
     * @param headClass   The {@link SCMHead} that the migration applies to.
     * @param revisionClass
     */
    protected SCMHeadMigration(Class<S> sourceClass, Class<H> headClass, Class<R> revisionClass) {
        this.sourceClass = sourceClass;
        this.headClass = headClass;
        this.revisionClass = revisionClass;
    }

    /**
     * Gets the {@link SCMHead} that the migration applies to.
     *
     * @return the {@link SCMHead} that the migration applies to.
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
     * Perform a migration.
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
     * @param head   the candidate head.
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
     * @param revision   the candidate revision.
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
