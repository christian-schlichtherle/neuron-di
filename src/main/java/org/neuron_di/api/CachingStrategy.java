package org.neuron_di.api;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.FixedValue;

/** Enumerates strategies for caching the return values of methods. */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the annotated method:
     * Although not strictly required, a subsequent call by any thread should
     * return another instance.
     */
    DISABLED {

        @Override
        FixedValue callback(FixedValue delegate) { return delegate; }
    },

    /**
     * Caches the return value of the annotated method:
     * Although not strictly required, a subsequent call by the same thread
     * should return the same instance.
     * A subsequent call by any other thread may return another instance.
     * This definition recursively applies to any thread.
     */
    NOT_THREAD_SAFE {

        @Override
        FixedValue callback(FixedValue delegate) {
            return new FixedValue() {

                // TODO: Consider removing `volatile` modifier.
                volatile Object returnValue;

                @Override
                public Object loadObject() throws Exception {
                    final Object value = returnValue;
                    return null != value
                            ? value
                            : (returnValue = delegate.loadObject());
                }
            };
        }
    },

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by the same thread must return the same instance.
     * Although not strictly required, a subsequent call by any other thread
     * should return another instance.
     * This definition recursively applies to any thread.
     */
    THREAD_LOCAL {

        @Override
        FixedValue callback(FixedValue delegate) {
            return new FixedValue() {

                final ThreadLocal<Object> results = new ThreadLocal<>();

                @Override
                public Object loadObject() throws Exception {
                    Object result = results.get();
                    if (null == result) {
                        results.set(result = delegate.loadObject());
                    }
                    return result;
                }
            };
        }
    },

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by any thread must return the same instance.
     */
    THREAD_SAFE {

        @Override
        FixedValue callback(FixedValue delegate) {
            return new FixedValue() {

                volatile Object returnValue;

                @Override
                public Object loadObject() throws Exception {
                    // Double checked locking:
                    Object value = returnValue;
                    if (null != value) {
                        return value;
                    } else {
                        synchronized (this) {
                            value = returnValue;
                            return null != value
                                    ? value
                                    : (returnValue = delegate.loadObject());
                        }
                    }
                }
            };
        }
    };

    abstract FixedValue callback(FixedValue delegate);
}
