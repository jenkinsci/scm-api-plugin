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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jenkins.scm.api.SCMSource;

public abstract class SCMSourceBuilder<B extends SCMSourceBuilder<B,S>,S extends SCMSource> {

    private final Class<S> clazz;
    private final String projectName;
    private final List<SCMSourceTrait> traits = new ArrayList<SCMSourceTrait>();

    public SCMSourceBuilder(Class<S> clazz, String projectName) {
        this.clazz = clazz;
        this.projectName = projectName;
    }

    public Class<S> sourceClass() {
        return clazz;
    }

    public String projectName() {
        return projectName;
    }

    public abstract S build();

    public List<SCMSourceTrait> traits() {
        return new ArrayList<SCMSourceTrait>(traits);
    }

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMNavigatorTrait trait) {
        trait.applyToBuilder(this);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withTrait(@NonNull SCMSourceTrait trait) {
        traits.add(trait);
        return (B) this;
    }

    public B withTraits(@NonNull SCMTrait<? extends SCMTrait<?>>... traits) {
        return withTraits(Arrays.asList(traits));
    }
    @SuppressWarnings("unchecked")
    public B withTraits(@NonNull Collection<? extends SCMTrait<?>> traits) {
        for (SCMTrait<?> trait : traits) {
            if (trait instanceof SCMNavigatorTrait) {
                withTrait((SCMNavigatorTrait)trait);
            } else if (trait instanceof SCMSourceTrait) {
                withTrait((SCMSourceTrait) trait);
            } else {
                throw new IllegalArgumentException("Unsupported trait: " + trait.getClass().getName());
            }
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withRequest(SCMNavigatorRequest request) {
        withTraits(request.traits());
        for (SCMSourceDecorator<?,?> decorator: request.decorators()) {
            decorator.applyTo(this, projectName());
        }
        return (B) this;
    }
}
