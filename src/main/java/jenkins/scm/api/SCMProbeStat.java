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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents the result of an existence check which may optionally include details of an alternative name for the
 * object being checked for existence.
 *
 * @since 2.0
 */
public class SCMProbeStat {

    /**
     * If the path does not exist, a non-{@code null} value may indicate the closest matching name.
     */
    @CheckForNull
    private String alternativePath;

    /**
     * The type of entity.
     */
    @NonNull
    private SCMFile.Type type;

    /**
     * Constructor.
     * @param type the type.
     * @param alternativePath the alternative path.
     */
    private SCMProbeStat(@NonNull SCMFile.Type type, @CheckForNull String alternativePath) {
        this.type = type;
        this.alternativePath = type != SCMFile.Type.NONEXISTENT ? null : alternativePath;
    }

    /**
     * Creates a {@link SCMProbeStat} from a {@link SCMFile.Type}.
     * @param type the {@link SCMFile.Type}.
     * @return the {@link SCMProbeStat}.
     */
    public static SCMProbeStat fromType(@NonNull SCMFile.Type type) {
        return new SCMProbeStat(type, null);
    }

    /**
     * Creates a {@link SCMProbeStat} from an alternative path suggestion.
     * @param alternativePath the suggested alternative path.
     * @return the {@link SCMProbeStat}.
     */
    public static SCMProbeStat fromAlternativePath(@NonNull String alternativePath) {
        return new SCMProbeStat(SCMFile.Type.NONEXISTENT, alternativePath);
    }

    /**
     * Shorthand for {@link #getType()}{@code != }{@link SCMFile.Type#NONEXISTENT}.
     *
     * @return {@link #getType()}{@code != }{@link SCMFile.Type#NONEXISTENT}.
     */
    public boolean exists() {
        return type != SCMFile.Type.NONEXISTENT;
    }

    /**
     * If {@link #getType()}{@code != }{@link SCMFile.Type#NONEXISTENT} and there is an alternative name that would
     * match but for case sensitivity or a typo.
     *
     * @return a suggested alternative name.
     */
    @CheckForNull
    public String getAlternativePath() {
        return alternativePath;
    }

    /**
     * The type of entity or {@link SCMFile.Type#NONEXISTENT} if the entity does not exist.
     *
     * @return the type of entity.
     */
    @NonNull
    public SCMFile.Type getType() {
        return type;
    }

}
