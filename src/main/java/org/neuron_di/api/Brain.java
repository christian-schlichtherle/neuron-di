package org.neuron_di.api;

import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
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

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    private final Objenesis objenesis = new ObjenesisStd();

    public static Brain build() { return new Brain(); }

    private Brain() { }

    public <T> T neuron(final Class<T> clazz) {
        Object instance;
        if (clazz.isAnnotationPresent(Neuron.class)) {
            instance = singletons.get(clazz);
            if (null == instance) {
                final CallbackHelper helper = new CallbackHelper(clazz, NO_INTERFACES) {

                    @Override
                    protected Object getCallback(final Method method) {
                        if (Modifier.isAbstract(method.getModifiers())) {
                            return (FixedValue) () -> neuron(method.getReturnType());
                        } else {
                            return NoOp.INSTANCE;
                        }
                    }
                };
                instance = Enhancer.create(clazz, NO_INTERFACES, helper,
                        helper.getCallbacks());
                if (clazz.isAnnotationPresent(Singleton.class)) {
                    final Object oldInstance = singletons.putIfAbsent(clazz, instance);
                    if (null != oldInstance) {
                        instance = oldInstance;
                    }
                }
            }
        } else {
            instance = objenesis.newInstance(clazz);
        }
        return clazz.cast(instance);
    }
}
