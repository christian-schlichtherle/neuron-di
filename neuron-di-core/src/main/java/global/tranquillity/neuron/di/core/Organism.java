package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.NoOp;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static global.tranquillity.neuron.di.core.Inspection.*;

public class Organism {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public static Organism breed() { return new Organism(); }

    private Organism() { }

    /**
     * Returns an instance of the given runtime class which will resolve its
     * dependencies lazily by recursively calling this method.
     */
    public <T> T make(Class<T> runtimeClass) {
        return make(runtimeClass, synapse -> make(synapse.getReturnType()));
    }

    /**
     * Returns an instance of the given runtime class which will resolve its
     * dependencies lazily by calling the given function.
     *
     * @param dependency a function which maps a synapse method to its resolved
     *                   dependency.
     */
    public <T> T make(final Class<T> runtimeClass,
                      final Function<Method, ?> dependency) {
        return inspect(runtimeClass).accept(null, new Visitor<T>() {

            @Override
            public T visitNeuron(final T nothing, final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                Object instance = singletons.get(runtimeClass);
                if (null == instance) {
                    instance = cglibAdapter((superclass, interfaces) -> {

                        class MethodVisitor
                                extends CallbackHelper
                                implements Visitor<Callback> {

                            private MethodVisitor() {
                                super(superclass, interfaces);
                            }

                            @Override
                            protected Callback getCallback(Method method) {
                                return element.inspect(method).accept(null, this);
                            }

                            @Override
                            public Callback visitSynapse(Callback nothing, SynapseElement element) {
                                final Method method = element.method();
                                return realCachingStrategy(element.cachingStrategy())
                                        .decorate(() -> dependency.apply(method));
                            }

                            @Override
                            public Callback visitMethod(Callback nothing, MethodElement element) {
                                return Optional
                                        .of(element.cachingStrategy())
                                        .map(Inspection::realCachingStrategy)
                                        .filter(RealCachingStrategy::isEnabled)
                                        .map(this::decorate)
                                        .orElse(NoOp.INSTANCE);
                            }

                            private Callback decorate(RealCachingStrategy s) {
                                return s.decorate((obj, method, args, proxy) ->
                                        proxy.invokeSuper(obj, args));
                            }
                        }

                        Object proxy = createProxy(superclass, interfaces, new MethodVisitor());
                        if (runtimeClass.isAnnotationPresent(Singleton.class)) {
                            final Object old = singletons.putIfAbsent(runtimeClass, proxy);
                            if (null != old) {
                                proxy = old;
                            }
                        }
                        return proxy;
                    })
                    .apply(runtimeClass);
                }
                return runtimeClass.cast(instance);
            }

            @Override
            public T visitClass(final T nothing, final ClassElement element) {
                assert runtimeClass == element.runtimeClass();
                return createInstance(runtimeClass);
            }
        });
    }
}
