package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static global.tranquillity.neuron.di.core.Inspection.cglibAdapter;

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

        class ClassVisitor implements Visitor {

            private Object instance;

            @Override
            public void visitNeuron(final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                instance = singletons.get(runtimeClass);
                if (null == instance) {
                    instance = cglibAdapter((superclass, interfaces) -> {

                        class MethodVisitor
                                extends CallbackHelper
                                implements Visitor {

                            private Callback callback;

                            private MethodVisitor() {
                                super(superclass, interfaces);
                            }

                            @Override
                            protected Callback getCallback(Method method) {
                                element.inspect(method).accept(this);
                                return callback;
                            }

                            @Override
                            public void visitSynapse(final SynapseElement element) {
                                final Method method = element.method();
                                callback = RealCachingStrategy
                                        .valueOf(element.cachingStrategy())
                                        .decorate(() -> dependency.apply(method));
                            }

                            @Override
                            public void visitMethod(MethodElement element) {
                                callback = Optional
                                        .of(element.cachingStrategy())
                                        .map(RealCachingStrategy::valueOf)
                                        .filter(RealCachingStrategy::isEnabled)
                                        .map(this::decorateMethodInterceptor)
                                        .orElse(NoOp.INSTANCE);
                            }

                            private Callback decorateMethodInterceptor(RealCachingStrategy strategy) {
                                return strategy.decorate((obj, method, args, proxy) ->
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
            }

            @Override
            public void visitClass(final ClassElement element) {
                assert runtimeClass == element.runtimeClass();
                instance = createInstance(runtimeClass);
            }
        }

        final ClassVisitor visitor = new ClassVisitor();
        Inspection.of(runtimeClass).accept(visitor);
        return runtimeClass.cast(visitor.instance);
    }

    private static Object createProxy(final Class<?> superclass,
                                      final Class<?>[] interfaces,
                                      final CallbackHelper helper) {
        final Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(helper);
        e.setCallbacks(helper.getCallbacks());
        return e.create();
    }

    private static <T> T createInstance(final Class<T> clazz) {
        try {
            final Constructor<T> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw (InstantiationError)
                    new InstantiationError(clazz.getName()).initCause(e);
        } catch (InstantiationException e) {
            throw (InstantiationError)
                    new InstantiationError(e.getMessage()).initCause(e);
        } catch (IllegalAccessException e) {
            throw (IllegalAccessError)
                    new IllegalAccessError(e.getMessage()).initCause(e);
        } catch (InvocationTargetException e) {
            sun.misc.Unsafe.getUnsafe().throwException(e.getTargetException());
            throw new AssertionError("Unreachable statement.", e);
        }
    }
}
