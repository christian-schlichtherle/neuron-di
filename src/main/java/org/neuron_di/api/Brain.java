package org.neuron_di.api;

import net.sf.cglib.proxy.*;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Brain {

    private static final Class[] NO_INTERFACES = new Class[0];

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
                final Class superClass;
                final Class[] interfaces;
                if (type.isInterface()) {
                    superClass = Object.class;
                    interfaces = new Class[] { type };
                } else {
                    superClass = type;
                    interfaces = NO_INTERFACES;
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
                                    .map(s -> (Callback) s.decorate((obj, method2, args, proxy) -> proxy.invokeSuper(obj, args)))
                                    .orElse(NoOp.INSTANCE);
                        }
                    }

                    boolean isAbstract(Method method) {
                        return Modifier.isAbstract(method.getModifiers());
                    }
                };
                instance = Enhancer.create(superClass, interfaces, helper,
                        helper.getCallbacks());
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
