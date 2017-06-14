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

/**
 * A contextual decorator of {@link SCMSourceBuilder} instances that can be used by a {@link SCMNavigatorTrait} for
 * example to apply {@link SCMSourceTrait}s to a subset of projects.
 *
 * @param <B> the type of {@link SCMSourceBuilder}.
 * @param <S> the type of {@link SCMSource}.
 */
public abstract class SCMSourceDecorator<B extends SCMSourceBuilder<B, S>, S extends SCMSource> {

    /**
     * A map of inferred {@link SCMSourceBuilder} classes to reduce the instantiation time cost of inferring the
     * builder class from the type parameters of the implementation class.
     */
    private static final WeakHashMap<Class<? extends SCMSourceDecorator>, Class<? extends SCMSourceBuilder>> builders
            = new WeakHashMap<Class<? extends SCMSourceDecorator>, Class<? extends SCMSourceBuilder>>();

    /**
     * The {@link B} class.
     */
    private final Class<B> builderClass;

    /**
     * Infers the type of the corresponding {@link SCMSourceBuilder} from the type parameters of the implementation
     * class. If the inference fails use {@link #SCMSourceDecorator(Class)}.
     */
    protected SCMSourceDecorator() {
        builderClass = SCMSourceDecorator.<B, S>inferBuilderClass(getClass());
    }

    /**
     * Bypasses {@link SCMSourceBuilder} type inference and specifies the type explicitly.
     *
     * @param builderClass the specialization of {@link SCMSourceBuilder} that this decorator applies to.
     */
    protected SCMSourceDecorator(Class<B> builderClass) {
        this.builderClass = builderClass;
    }

    /**
     * Infers the type of builder class from the type of {@link SCMSourceDecorator}.
     *
     * @param clazz the type of {@link SCMSourceDecorator}
     * @param <B>   coerces types by cheating (but this cheating is OK as we are deriving from type parameters)
     * @param <S>   coerces types by cheating (but this cheating is OK as we are deriving from type parameters)
     * @return the specialization of {@link SCMSourceBuilder} that the specified {@link SCMSourceDecorator} applies to.
     */
    @SuppressWarnings("unchecked")
    private static <B extends SCMSourceBuilder<B, S>, S extends SCMSource> Class<B> inferBuilderClass(
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

    /**
     * Applies this decorator to the specified {@link SCMSourceBuilder} for the supplied project name.
     * If this {@link SCMSourceDecorator} is not specialized for the type of {@link SCMSourceBuilder} then this will
     * be a no-op.
     *
     * @param builder     the {@link SCMSourceBuilder}.
     * @param projectName the project name.
     */
    public final void applyTo(SCMSourceBuilder<?, ?> builder, String projectName) {
        if (builderClass.isInstance(builder)) {
            // we guard the SPI so that implementations can avoid casting while the API is generic so that
            // consumers do not need to care about checking individual decorators for "fit"
            decorate(builderClass.cast(builder), projectName);
        }
    }

    /**
     * SPI: decorate the supplied builder for creation of the named project.
     * @param builder the builder to decorate.
     * @param projectName the project name.
     */
    protected abstract void decorate(B builder, String projectName);
}
