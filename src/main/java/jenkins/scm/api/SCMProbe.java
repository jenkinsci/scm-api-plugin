/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc., Stephen Connolly.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import java.io.Closeable;
import java.io.IOException;

/**
 * An unmanaged {@link SCMSourceCriteria.Probe} that has its lifecycle managed outside of
 * {@link SCMSource#fetch(TaskListener)}. A {@link SCMProbe} is used to check and recheck {@link SCMSourceCriteria}
 * so implementations are strongly recommended to provide caching of results from {@link #stat(String)} and
 * {@link #lastModified()}.
 *
 * @since FIXME
 */
public abstract class SCMProbe extends SCMSourceCriteria.Probe implements Closeable {
    /**
     * Checks if the path, relative to the head candidate root, exists or not. The results of this method should
     * be cached where possible but can involve a remote network call.
     *
     * @param path the path.
     * @return {@code true} iff the path exists (may be a file or a directory or a symlink or whatever).
     * @throws IOException if a remote network call failed and the result is therefore indeterminate.
     * @deprecated use {@link #stat(String)}
     */
    @Deprecated
    public final boolean exists(@NonNull String path) throws IOException {
        return stat(path).exists();
    }

    /**
     * Checks if the path, relative to the head candidate root, exists or not. The results of this method should
     * be cached where possible but can involve a remote network call.
     *
     * @param path the path.
     * @return The results of the check.
     * @throws IOException if a remote network call failed and the result is therefore indeterminate.
     */
    @NonNull
    public abstract SCMProbeStat stat(@NonNull String path) throws IOException;

}
