package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author Stephen Connolly
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
    public boolean exists(@NonNull String path) throws IOException {
        return stat(path).isExists();
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
