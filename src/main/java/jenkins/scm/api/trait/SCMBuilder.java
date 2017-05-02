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

public abstract class SCMBuilder<B extends SCMBuilder<B,S>,S extends SCM> {

    private final Class<S> clazz;
    private SCMHead head;
    private SCMRevision revision;

    public SCMBuilder(Class<S> clazz, @NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        this.clazz = clazz;
        this.head = head;
        this.revision = revision;
    }

    public Class<S> scmClass() {
        return clazz;
    }

    public SCMHead head() {
        return head;
    }

    public B withHead(@NonNull SCMHead head) {
        this.head = head;
        return (B) this;
    }

    public SCMRevision revision() {
        return revision;
    }

    public B withRevision(@CheckForNull SCMRevision revision) {
        this.revision = revision;
        return (B) this;
    }

    public abstract S build();

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        trait.applyToBuilder(this);
        return (B) this;
    }

    public B withTraits(@NonNull SCMSourceTrait... traits) {
        return withTraits(Arrays.asList(traits));
    }

    @SuppressWarnings("unchecked")
    public B withTraits(@NonNull Collection<SCMSourceTrait> traits) {
        for (SCMSourceTrait trait : traits) {
            withTrait(trait);
        }
        return (B) this;
    }
}
