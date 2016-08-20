package global.tranquillity.neuron.di.api;

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

import static global.tranquillity.neuron.di.api.CachingStrategy.DISABLED;

public class Organism {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public static Organism breed() { return new Organism(); }

    private Organism() { }

    /**
     * Returns an instance of the given type which will resolve its dependencies
     * lazily.
     */
    public <T> T make(final Class<T> type) {
        return inspect(type).accept(null, new Visitor<T>() {

            @Override
            public T visitNeuron(final T ignored, final NeuronElement element) {
                assert type == element.clazz();
                Object instance;
                instance = singletons.get(type);
                if (null == instance) {
                    final Class<?> superClass;
                    final Class<?>[] interfaces;
                    if (type.isInterface()) {
                        if (hasDefaultMethodsWhichRequireCaching(type)) {
                            superClass = createClass(type);
                            interfaces = NO_CLASSES;
                        } else {
                            superClass = Object.class;
                            interfaces = new Class<?>[] { type };
                        }
                    } else {
                        superClass = type;
                        interfaces = NO_CLASSES;
                    }
                    final CallbackHelper helper = new CallbackHelper(superClass, interfaces) {

                        @Override
                        protected Callback getCallback(final Method method) {
                            if (isParameterless(method)) {
                                final Optional<CachingStrategy> maybeCachingStrategy = maybeCachingStrategy(method);
                                if (isAbstract(method)) {
                                    final Class<?> returnType = method.getReturnType();
                                    return maybeCachingStrategy
                                            .orElseGet(element::cachingStrategy)
                                            .decorate(() -> make(returnType));
                                } else {
                                    return maybeCachingStrategy
                                            .filter(CachingStrategy::isEnabled)
                                            .map(s -> s.decorate((obj, ignored, args, proxy) -> proxy.invokeSuper(obj, args)))
                                            .orElse(NoOp.INSTANCE);
                                }
                            } else {
                                return NoOp.INSTANCE;
                            }
                        }
                    };
                    instance = createProxy(superClass, interfaces, helper);
                    if (type.isAnnotationPresent(Singleton.class)) {
                        final Object oldInstance = singletons.putIfAbsent(type, instance);
                        if (null != oldInstance) {
                            instance = oldInstance;
                        }
                    }
                }
                return type.cast(instance);
            }

            @Override
            public T visitClass(final T ignored, final ClassElement element) {
                assert type == element.clazz();
                return createInstance(type);
            }
        });
    }

    public static ClassElement inspect(final Class<?> clazz) {

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

                @Override
                public <V> V traverse(V value, final Visitor<V> visitor) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        value = inspect(method).accept(value, visitor);
                    }
                    return value;
                }

                MethodElement inspect(final Method method) {

                    class MethodBase {

                        CachingStrategy cachingStrategy;

                        public CachingStrategy cachingStrategy() {
                            return cachingStrategy;
                        }

                        public Method method() { return method; }
                    }

                    class RealSynapseElement extends MethodBase implements SynapseElement {

                        RealSynapseElement(final CachingStrategy cachingStrategy) {
                            super.cachingStrategy = cachingStrategy;
                        }
                    }

                    class RealMethodElement extends MethodBase implements MethodElement {

                        RealMethodElement(final CachingStrategy cachingStrategy) {
                            super.cachingStrategy = cachingStrategy;
                        }
                    }

                    if (isParameterless(method)) {
                        final Optional<CachingStrategy> maybeCachingStrategy = maybeCachingStrategy(method);
                        if (isAbstract(method)) {
                            return new RealSynapseElement(maybeCachingStrategy
                                    .orElseGet(neuron::cachingStrategy));
                        } else {
                            return new RealMethodElement(maybeCachingStrategy
                                    .orElse(DISABLED));
                        }
                    } else {
                        return new RealMethodElement(DISABLED);
                    }
                }
            }

            return new RealNeuronElement();
        } else {

            class RealClassElement extends ClassBase implements ClassElement { }

            return new RealClassElement();
        }
    }

    private static boolean isParameterless(Method method) {
        return 0 == method.getParameterCount();
    }

    private static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    private static Optional<CachingStrategy> maybeCachingStrategy(Method method) {
        return Optional
                .ofNullable(method.getAnnotation(Caching.class))
                .map(Caching::value);
    }

    private static boolean hasDefaultMethodsWhichRequireCaching(final Class<?> iface) {
        assert iface.isInterface();
        for (final Method method : iface.getDeclaredMethods()) {
            if (method.isDefault()) {
                final Caching caching = method.getAnnotation(Caching.class);
                if (null != caching && caching.value().isEnabled()){
                    return true;
                }
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
}
