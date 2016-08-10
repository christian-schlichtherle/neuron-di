package org.neuron_di.api;

import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Brain {

    private static final Class[] NO_INTERFACES = new Class[0];

    private static final Synapse defaultSynapse;

    static {
        try {
            defaultSynapse = Dummy.class
                    .getDeclaredMethod("dummy")
                    .getAnnotation(Synapse.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

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
        if (type.isAnnotationPresent(Neuron.class)) {
            instance = singletons.get(type);
            if (null == instance) {
                final CallbackHelper helper = new CallbackHelper(type, NO_INTERFACES) {

                    @Override
                    protected Object getCallback(final Method method) {
                        if (Modifier.isAbstract(method.getModifiers()) &&
                                0 == method.getParameterCount()) {
                            final Synapse synapse = method.getAnnotation(Synapse.class);
                            return (null != synapse ? synapse: defaultSynapse)
                                    .cachingStrategy()
                                    .callback(Brain.this, method);
                        } else {
                            return NoOp.INSTANCE;
                        }
                    }
                };
                instance = Enhancer.create(type, NO_INTERFACES, helper,
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
     * {@linkplain Synapse synapses} and use {@link CachingStrategy#THREAD_SAFE}
     * in order to enable stubbing and verifying the mocked dependencies.
     */
    public <T> T test(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Neuron
    private abstract static class Dummy {

        @Synapse
        abstract Dummy dummy();
    }
}
