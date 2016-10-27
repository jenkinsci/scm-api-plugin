package jenkins.scm.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents the result of an existence check.
 */
public class SCMProbeStat {

    /**
     * If the path does not exist, a non-{@code null} value may indicate the closest matching name.
     */
    @CheckForNull
    private String alternativePath;

    /**
     * If the path does exist, indicates that the type of entity.
     */
    @NonNull
    private Type type;

    private SCMProbeStat(@NonNull Type type, @CheckForNull String alternativePath) {
        this.type = type;
        this.alternativePath = type != Type.NONEXISTENT ? null : alternativePath;
    }

    public static SCMProbeStat found(@NonNull Type type) {
        return new SCMProbeStat(type, null);
    }

    public static SCMProbeStat missing(@CheckForNull String alternativePath) {
        return new SCMProbeStat(Type.NONEXISTENT, alternativePath);
    }

    public static SCMProbeStat missing() {
        return new SCMProbeStat(Type.NONEXISTENT, null);
    }

    public boolean isExists() {
        return type != Type.NONEXISTENT;
    }

    @CheckForNull
    public String getAlternativePath() {
        return alternativePath;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    public enum Type {
        NONEXISTENT,
        UNKNOWN,
        REGULAR_FILE,
        DIRECTORY,
        LINK;
    }
}
