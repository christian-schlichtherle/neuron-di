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
     * Returns an instance of the given type which will resolve its dependencies
     * lazily.
     */
    public <T> T make(final Class<T> clazz) {
        return inspect(clazz).accept(null, new Visitor<T>() {

            @Override
            public T visitNeuron(final T nothing, final NeuronElement element) {
                assert clazz == element.clazz();
                Object instance = singletons.get(clazz);
                if (null == instance) {
                    instance = cglibAdapter((superClass, interfaces) -> {

                        class MethodVisitor
                                extends CallbackHelper
                                implements Visitor<Callback> {

                            private MethodVisitor() {
                                super(superClass, interfaces);
                            }

                            @Override
                            protected Callback getCallback(Method method) {
                                return element.inspect(method).accept(null, this);
                            }

                            @Override
                            public Callback visitSynapse(Callback nothing, SynapseElement element) {
                                final Class<?> returnType = element.methodReturnType();
                                return realCachingStrategy(element.cachingStrategy())
                                        .decorate(() -> make(returnType));
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

                        Object proxy = createProxy(superClass, interfaces, new MethodVisitor());
                        if (clazz.isAnnotationPresent(Singleton.class)) {
                            final Object old = singletons.putIfAbsent(clazz, proxy);
                            if (null != old) {
                                proxy = old;
                            }
                        }
                        return proxy;
                    })
                    .apply(clazz);
                }
                return clazz.cast(instance);
            }

            @Override
            public T visitClass(final T nothing, final ClassElement element) {
                assert clazz == element.clazz();
                return createInstance(clazz);
            }
        });
    }

    public static Element inspect(final Class<?> clazz) {

        class ClassBase {

            public Class<?> clazz() { return clazz; }
        }

        final Neuron neuron = clazz.getAnnotation(Neuron.class);
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
        return new Function<Class<?>, V>() {

            @Override
            public V apply(final Class<?> clazz) {
                final Class<?> superClass;
                final Class<?>[] interfaces;
                if (clazz.isInterface()) {
                    if (hasDefaultMethodsWhichRequireCaching(clazz)) {
                        superClass = createClass(clazz);
                        interfaces = NO_CLASSES;
                    } else {
                        superClass = Object.class;
                        interfaces = new Class<?>[]{clazz};
                    }
                } else {
                    superClass = clazz;
                    interfaces = NO_CLASSES;
                }
                return function.apply(superClass, interfaces);
            }
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

    private static Object createProxy(final Class<?> superClass,
                                      final Class<?>[] interfaces,
                                      final CallbackHelper helper) {
        final Enhancer e = new Enhancer();
        e.setSuperclass(superClass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(helper);
        e.setCallbacks(helper.getCallbacks());
        return e.create();
    }

    private static <T> T createInstance(final Class<T> type) {
        try {
            final Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw (InstantiationError)
                    new InstantiationError(type.getName()).initCause(e);
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
