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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.WeakHashMap;
import jenkins.scm.api.SCMSource;
import org.jvnet.tiger_types.Types;

public abstract class SCMSourceDecorator<B extends SCMSourceBuilder<B, S>, S extends SCMSource> {

    private static final WeakHashMap<Class<? extends SCMSourceDecorator>, Class<? extends SCMSourceBuilder>> builders
            = new WeakHashMap<Class<? extends SCMSourceDecorator>, Class<? extends SCMSourceBuilder>>();

    private final Class<B> builderClass;

    protected SCMSourceDecorator() {
        builderClass = SCMSourceDecorator.<B,S>lookupBuilderClass(getClass());
    }

    protected SCMSourceDecorator(Class<B> builderClass) {
        this.builderClass = builderClass;
    }

    @SuppressWarnings("unchecked")
    private static <B extends SCMSourceBuilder<B, S>, S extends SCMSource> Class<B> lookupBuilderClass(
            Class<? extends SCMSourceDecorator> clazz) {
        synchronized (builders) {
            Class result = builders.get(clazz);
            if (result == null) {
                Type bt = Types.getBaseClass(clazz, SCMSourceDecorator.class);
                if (bt instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) bt;
                    // this 'result' is the closest approximation of B of SCMSourceDecorator<B,S>.
                    result = Types.erasure(pt.getActualTypeArguments()[0]);
                    if (!SCMSourceBuilder.class.isAssignableFrom(result)) {
                        throw new AssertionError(
                                "SCMSourceBuilder inference failed for " + clazz.getName()
                                        + ", try using the constructor that takes an explicit class argument"
                        );
                    }
                } else {
                    result = SCMSourceBuilder.class;
                }
                builders.put(clazz, result);
            }
            return result;
        }
    }

    public final void applyTo(SCMSourceBuilder<?, ?> builder, String projectName) {
        if (builderClass.isInstance(builder)) {
            decorate(builderClass.cast(builder), projectName);
        }
    }

    protected abstract void decorate(B builder, String projectName);
}
