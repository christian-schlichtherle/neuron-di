package org.neuron_di.api;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.neuron_di.api.CachingStrategy.DISABLED;

public class Brain {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    private final Objenesis objenesis = new ObjenesisStd();

    public static Brain build() { return new Brain(); }

    private Brain() { }

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
                                    .filter(strategy -> strategy != DISABLED)
                                    .map(s -> s.decorate((obj, method2, args, proxy) -> proxy.invokeSuper(obj, args)))
                                    .orElse(NoOp.INSTANCE);
                        }
                    }

                    boolean isAbstract(Method method) {
                        return Modifier.isAbstract(method.getModifiers());
                    }
                };
                instance = create(superClass, interfaces, helper);
                if (type.isAnnotationPresent(Singleton.class)) {
                    final Object oldInstance = singletons.putIfAbsent(type, instance);
                    if (null != oldInstance) {
                        instance = oldInstance;
                    }
                }
            }
        } else {
            instance = objenesis.newInstance(type);
        }
        assert null != instance;
        return type.cast(instance);
    }

    private static boolean hasDefaultMethodsWhichRequireCaching(final Class<?> iface) {
        assert iface.isInterface();
        for (final Method method : iface.getDeclaredMethods()) {
            if (method.isDefault()) {
                final Caching caching = method.getAnnotation(Caching.class);
                if (null != caching && caching.value() != DISABLED){
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

    private static Object create(final Class<?> superClass,
                                 final Class<?>[] interfaces,
                                 final CallbackHelper helper) {
        final Enhancer e = new Enhancer();
        e.setSuperclass(superClass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(helper);
        e.setCallbacks(helper.getCallbacks());
        return e.create();
    }

    /**
     * Returns an instance of the given type which will resolve it's
     * non-constant dependencies to mock objects for testing.
     * The instance will ignore any caching strategy configured for its
     * {@linkplain Caching synapses} and use {@link org.neuron_di.api.CachingStrategy#THREAD_SAFE}
     * in order to enable stubbing and verifying the mocked dependencies.
     */
    public <T> T test(Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
