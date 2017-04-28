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
