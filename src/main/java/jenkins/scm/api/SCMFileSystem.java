/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
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
import hudson.ExtensionPoint;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import java.io.IOException;

/**
 * A virtual file system for a specific {@link SCM} potentially pinned to a specific {@link SCMRevision}.
 *
 * @author Stephen Connolly
 */
public abstract class SCMFileSystem {

    @CheckForNull
    private final SCMRevision rev;

    protected SCMFileSystem(@CheckForNull SCMRevision rev) {
        this.rev = rev;
    }

    /**
     * If this inspector is looking at the specific commit,
     * returns that revision. Otherwise null, indicating
     * that the inspector is looking at just the latest state
     * of the repository.
     */
    @CheckForNull
    public SCMRevision getRevision() {
        return rev;
    }

    /**
     * Whether this inspector is looking at the specific commit.
     * <p/>
     * Short for {@code getRevision()!=null}.
     */
    public final boolean isFixedRevision() {
        return getRevision() != null;
    }

    /**
     * Short for {@code getRoot().get(path)}
     */
    @CheckForNull
    public final SCMFile get(@NonNull String path) throws IOException {
        return getRoot().get(path);
    }

    /**
     * Returns the {@link SCMFile} object that represents the root directory of the repository.
     */
    @NonNull
    public abstract SCMFile getRoot() throws IOException;

    /**
     * Given a {@link SCM} this method will try to retrieve a corresponding {@link SCMFileSystem} instance.
     *
     * @param scm the {@link SCM}.
     * @return the corresponding {@link SCMFileSystem} or {@code null} if there is none.
     */
    @CheckForNull
    public static SCMFileSystem of(@NonNull SCM scm) {
        return of(scm, null);
    }

    /**
     * Given a {@link SCM} this method will try to retrieve a corresponding {@link SCMFileSystem} instance that
     * reflects the content at the specified {@link SCMRevision}.
     *
     * @param scm the {@link SCM}.
     * @param rev the specified {@link SCMRevision}.
     * @return the corresponding {@link SCMFileSystem} or {@code null} if there is none.
     */
    @CheckForNull
    public static SCMFileSystem of(@NonNull SCM scm, @CheckForNull SCMRevision rev) {
        scm.getClass(); // throw NPE if null
        SCMFileSystem fallBack = null;
        Jenkins j = Jenkins.getInstance();
        if (j == null) {
            return fallBack;
        }
        for (Builder b : j.getExtensionList(Builder.class)) { // TODO 1.572+ ExtensionList.lookup
            SCMFileSystem inspector = b.build(scm, rev);
            if (inspector != null) {
                if (inspector.isFixedRevision()) {
                    return inspector;
                }
                if (fallBack == null) {
                    fallBack = inspector;
                }
            }
        }
        return fallBack;
    }

    /**
     * Given a {@link SCMSource} and a {@link SCMHead} this method will try to retrieve a corresponding
     * {@link SCMFileSystem} instance that reflects the content of the specified {@link SCMHead}.
     *
     * @param source the {@link SCMSource}.
     * @param head   the specified {@link SCMHead}.
     * @return the corresponding {@link SCMFileSystem} or {@code null} if there is none.
     */
    @CheckForNull
    public static SCMFileSystem of(@NonNull SCMSource source, @NonNull SCMHead head) {
        return of(source, head, null);
    }

    /**
     * Given a {@link SCMSource}, a {@link SCMHead} and a {@link SCMRevision} this method will try to retrieve a
     * corresponding {@link SCMFileSystem} instance that reflects the content of the specified {@link SCMHead} at the
     * specified {@link SCMRevision}.
     *
     * @param source the {@link SCMSource}.
     * @param head   the specified {@link SCMHead}.
     * @param rev    the specified {@link SCMRevision}.
     * @return the corresponding {@link SCMFileSystem} or {@code null} if there is none.
     */
    @CheckForNull
    public static SCMFileSystem of(@NonNull SCMSource source, @NonNull SCMHead head,
                                   @CheckForNull SCMRevision rev) {
        source.getClass(); // throw NPE if null
        SCMFileSystem fallBack = null;
        Jenkins j = Jenkins.getInstance();
        if (j == null) {
            return fallBack;
        }
        for (Builder b : j.getExtensionList(Builder.class)) { // TODO 1.572+ ExtensionList.lookup
            SCMFileSystem inspector = b.build(source, head, rev);
            if (inspector != null) {
                if (inspector.isFixedRevision()) {
                    return inspector;
                }
                if (fallBack == null) {
                    fallBack = inspector;
                }
            }
        }
        return fallBack;
    }

    /**
     * Extension point that allows different plugins to implement {@link SCMFileSystem} classes for the same {@link SCM}
     * or {@link SCMSource} and let Jenkins pick the most capable for any specific {@link SCM} implementation.
     */
    public abstract static class Builder implements ExtensionPoint {

        /**
         * Given a {@link SCM} this should try to build a corresponding {@link SCMFileSystem} instance that
         * reflects the content at the specified {@link SCMRevision}. If the {@link SCM} is supported but not
         * for a fixed revision, best effort is acceptable as the most capable {@link SCMFileSystem} will be returned
         * to the caller.
         *
         * @param scm the {@link SCM}.
         * @param rev the specified {@link SCMRevision}.
         * @return the corresponding {@link SCMFileSystem} or {@code null} if this builder cannot create a {@link
         *         SCMFileSystem} for the specified {@link SCM}.
         */
        @CheckForNull
        public abstract SCMFileSystem build(@NonNull SCM scm, @CheckForNull SCMRevision rev);

        /**
         * Given a {@link SCMSource}, a {@link SCMHead} and a {@link SCMRevision} this method should try to build a
         * corresponding {@link SCMFileSystem} instance that reflects the content of the specified {@link SCMHead} at
         * the specified {@link SCMRevision}. If the {@link SCMSource} is supported but not for a fixed revision,
         * best effort is acceptable as the most capable {@link SCMFileSystem} will be returned
         * to the caller.
         *
         * @param source the {@link SCMSource}.
         * @param head   the specified {@link SCMHead}.
         * @param rev    the specified {@link SCMRevision}.
         * @return the corresponding {@link SCMFileSystem} or {@code null} if there is none.
         */
        @CheckForNull
        public SCMFileSystem build(@NonNull SCMSource source, @NonNull SCMHead head,
                                   @CheckForNull SCMRevision rev) {
            return build(source.build(head, rev), rev);
        }
    }
}
