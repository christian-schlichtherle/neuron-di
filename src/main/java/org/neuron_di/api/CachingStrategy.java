package org.neuron_di.api;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.FixedValue;

import java.lang.reflect.Method;

/**
 * Enumerates strategies for caching dependencies at an injection point
 * (synapse).
 */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the annotated method:
     * A subsequent call by any thread may return another instance.
     */
    DISABLED {

        @Override
        Callback callback(Brain brain, Method method) {
            return (FixedValue) () -> brain.make(method.getReturnType());
        }
    },

    /**
     * Caches the return value of the annotated method:
     * Although not strictly required, a subsequent call by the same thread
     * should return the same instance.
     * A subsequent call by any other thread may return another instance.
     * This definition recursively applies to all threads.
     */
    NOT_THREAD_SAFE {

        @Override
        Callback callback(Brain brain, Method method) {
            return new FixedValue() {

                // TODO: Consider removing `volatile` modifier.
                volatile Object returnValue;

                @Override
                public Object loadObject() {
                    final Object value = returnValue;
                    return null != value
                            ? value
                            : (returnValue = brain.make(method.getReturnType()));
                }
            };
        }
    },

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by the same thread must return the same instance.
     * Although not strictly required, a subsequent call by any other thread
     * should return another instance.
     * This definition recursively applies to all threads.
     */
    THREAD_LOCAL {

        @Override
        Callback callback(Brain brain, Method method) {
            return new FixedValue() {

                final ThreadLocal<Object> returnValues = new ThreadLocal<Object>() {
                    @Override
                    protected Object initialValue() {
                        return brain.make(method.getReturnType());
                    }
                };

                @Override
                public Object loadObject() { return returnValues.get(); }
            };
        }
    },

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by any thread must return the same instance.
     */
    THREAD_SAFE {

        @Override
        Callback callback(Brain brain, Method method) {
            return new FixedValue() {

                volatile Object returnValue;

                @Override
                public Object loadObject() {
                    // Double checked locking:
                    Object value = returnValue;
                    if (null != value) {
                        return value;
                    } else {
                        final Class<?> returnType = method.getReturnType();
                        synchronized (this) {
                            value = returnValue;
                            return null != value
                                    ? value
                                    : (returnValue = brain.make(returnType));
                        }
                    }
                }
            };
        }
    };

    abstract Callback callback(Brain brain, Method method);
}
