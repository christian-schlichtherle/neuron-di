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
import java.util.function.BiFunction;

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
                Object instance;
                instance = singletons.get(clazz);
                if (null == instance) {
                    instance = mapSuperClassAndInterfaces(clazz, new BiFunction<Class<?>, Class<?>[], Object>() {

                        @Override
                        public Object apply(final Class<?> superClass, final Class<?>[] interfaces) {

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
                                    final Class<?> returnType = element.method().getReturnType();
                                    return element.cachingStrategy().decorate(() -> make(returnType));
                                }

                                @Override
                                public Callback visitMethod(Callback nothing, MethodElement element) {
                                    return Optional
                                            .of(element.cachingStrategy())
                                            .filter(CachingStrategy::isEnabled)
                                            .map(this::decorate)
                                            .orElse(NoOp.INSTANCE);
                                }

                                private Callback decorate(CachingStrategy s) {
                                    return s.decorate((obj, method, args, proxy) ->
                                            proxy.invokeSuper(obj, args));
                                }
                            }

                            Object instance = createProxy(superClass, interfaces, new MethodVisitor());
                            if (clazz.isAnnotationPresent(Singleton.class)) {
                                final Object oldInstance = singletons.putIfAbsent(clazz, instance);
                                if (null != oldInstance) {
                                    instance = oldInstance;
                                }
                            }
                            return instance;
                        }
                    });
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
            }

            return new RealNeuronElement();
        } else {

            class RealClassElement extends ClassBase implements ClassElement { }

            return new RealClassElement();
        }
    }

    private static <R> R mapSuperClassAndInterfaces(
            final Class<?> clazz,
            final BiFunction<Class<?>, Class<?>[], R> mapper) {
        final Class<?> superClass;
        final Class<?>[] interfaces;
        if (clazz.isInterface()) {
            if (hasDefaultMethodsWhichRequireCaching(clazz)) {
                superClass = createClass(clazz);
                interfaces = NO_CLASSES;
            } else {
                superClass = Object.class;
                interfaces = new Class<?>[] { clazz };
            }
        } else {
            superClass = clazz;
            interfaces = NO_CLASSES;
        }
        return mapper.apply(superClass, interfaces);
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
