package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.CachingStrategy;
import global.tranquillity.neuron.di.api.Neuron;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import sun.misc.Unsafe;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Organism {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

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
                                        .map(Organism::realCachingStrategy)
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

    public static Element inspect(final Class<?> runtimeClass) {

        class ClassBase {

            public Class<?> runtimeClass() { return runtimeClass; }
        }

        final Neuron neuron = runtimeClass.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement extends ClassBase implements NeuronElement {

                @Override
                public CachingStrategy cachingStrategy() {
                    return neuron.cachingStrategy();
                }
            }

            return new RealNeuronElement();
        } else {

            class RealClassElement extends ClassBase implements ClassElement { }

            return new RealClassElement();
        }
    }

    /**
     * Adapts a function which accepts a class object reflecting a super class
     * and an array of class objects reflecting interfaces to a function which
     * accepts a class object reflecting a class or interface.
     * <p>
     * The given function is supposed to call some CGLIB method, which typically
     * accept a super class and an array of interfaces whereas the returned
     * function accepts a class or interface.
     *
     * @param function a function which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     *                 Returns an arbitrary value.
     * @param <V> the return type of {@code function}.
     * @return a function which accepts a class object reflecting a class or
     *         interface.
     *         Returns the return value of {@code function}.
     */
    static <V> Function<Class<?>, V> cglibAdapter(
            final BiFunction<Class<?>, Class<?>[], V> function) {
        return runtimeClass -> {
            final Class<?> superclass;
            final Class<?>[] interfaces;
            if (runtimeClass.isInterface()) {
                if (hasDefaultMethodsWhichRequireCaching(runtimeClass)) {
                    superclass = createClass(runtimeClass);
                    interfaces = NO_CLASSES;
                } else {
                    superclass = Object.class;
                    interfaces = new Class<?>[]{runtimeClass};
                }
            } else {
                superclass = runtimeClass;
                interfaces = NO_CLASSES;
            }
            return function.apply(superclass, interfaces);
        };
    }

    private static boolean hasDefaultMethodsWhichRequireCaching(final Class<?> iface) {
        assert iface.isInterface();
        for (final Method method : iface.getDeclaredMethods()) {
            if (method.isDefault()) {
                return cachingStrategyOption(method)
                        .map(Organism::realCachingStrategy)
                        .filter(RealCachingStrategy::isEnabled)
                        .isPresent();
            }
        }
        for (final Class<?> superInterface : iface.getInterfaces()) {
            if (hasDefaultMethodsWhichRequireCaching(superInterface)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> createClass(final Class<?> iface) {
        assert iface.isInterface();
        final Enhancer e = new Enhancer();
        e.setSuperclass(Object.class);
        e.setInterfaces(new Class<?>[] { iface });
        e.setCallbackType(NoOp.class);
        return e.createClass();
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
            Unsafe.getUnsafe().throwException(e.getTargetException());
            throw new AssertionError("Unreachable statement.", e);
        }
    }

    private static RealCachingStrategy realCachingStrategy(CachingStrategy strategy) {
        return RealCachingStrategy.valueOf(strategy.name());
    }

    static boolean isParameterless(Method method) {
        return 0 == method.getParameterCount();
    }

    static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    static Optional<CachingStrategy> cachingStrategyOption(Method method) {
        return Optional
                .ofNullable(method.getAnnotation(Caching.class))
                .map(Caching::value);
    }
}
