package global.tranquillity.neuron.di.api;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import javax.inject.Singleton;
import java.lang.reflect.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        Object instance;
        final Neuron neuron = type.getAnnotation(Neuron.class);
        if (null != neuron) {
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
                        final Optional<CachingStrategy> maybeCachingStrategy = Optional
                                .ofNullable(method.getAnnotation(Caching.class))
                                .filter(ignored -> 0 == method.getParameterCount())
                                .map(Caching::value);
                        if (isAbstract(method)) {
                            final Class<?> returnType = method.getReturnType();
                            return maybeCachingStrategy
                                    .orElseGet(neuron::caching)
                                    .decorate(() -> make(returnType));
                        } else {
                            return maybeCachingStrategy
                                    .filter(CachingStrategy::isEnabled)
                                    .map(s -> s.decorate((obj, method2, args, proxy) -> proxy.invokeSuper(obj, args)))
                                    .orElse(NoOp.INSTANCE);
                        }
                    }

                    boolean isAbstract(Method method) {
                        return Modifier.isAbstract(method.getModifiers());
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
        } else {
            instance = createInstance(type);
        }
        assert null != instance;
        return type.cast(instance);
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

    private static <T> Object createInstance(final Class<T> type) {
        try {
            final Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException | InstantiationException e) {
            throw new UndeclaredThrowableException(e);
        } catch (InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        }
    }
}
