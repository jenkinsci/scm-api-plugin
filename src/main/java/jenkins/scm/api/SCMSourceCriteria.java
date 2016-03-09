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
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * Filter that selects heads picked up by {@link SCMSource} out of all the branches and other heads
 * found in the repository.
 */
public interface SCMSourceCriteria extends Serializable {

    /**
     * Validates if a potential head is actually a head.
     *
     * @param probe    the {@link Probe} for the head candidate.
     * @param listener a listener which may receive informational messages explaining why a head was accepted or
     *                 rejected
     * @return {@code true} iff the candidate should be included in the list of heads
     *         built by Jenkins.
     * @throws IOException if an error occurs while performing the operation.
     */
    boolean isHead(@NonNull Probe probe, @NonNull TaskListener listener) throws IOException;

    /**
     * A probe for a branch candidate. Inspectors can tell whether a file path exists.
     */
    static abstract class Probe implements Serializable {

        /**
         * Returns the name of the potential head.
         *
         * @return the name of the potential head.
         */
        public abstract String name();

        /**
         * Returns the time that the potential head was last modified.
         *
         * @return A <code>long</code> value representing the time the file was
         *         last modified, measured in milliseconds since the epoch
         *         (00:00:00 GMT, January 1, 1970)
         */
        public abstract long lastModified();

        /**
         * Checks if the path, relative to the head candidate root, exists or not. The results of this method should
         * be cached where possible but can involve a remote network call.
         *
         * @param path the path.
         * @return {@code true} iff the path exists (may be a file or a directory or a symlink or whatever).
         * @throws IOException if a remote network call failed and the result is therefore indeterminate.
         */
        public abstract boolean exists(@NonNull String path) throws IOException;

        /**
         * Returns the {@link SCMFile} of the root of this head candidate if such deep introspection can be
         * cheaply provided by the version control system in question.
         * <p>When available, this provides more capabilities to analyze what's in the repository.
         * Given the frequency of {@link SCMSourceCriteria#isHead(SCMSourceCriteria.Probe,
         * hudson.model.TaskListener)} call, this method needs to be used with caution.</p>
         *
         * @return the {@link SCMFile} of the root of this head candidate or {@code null} if this is not available
         *         or would require remote network calls.
         */
        @CheckForNull
        public SCMFile getRoot() {
            return null;
        }
    }
}
