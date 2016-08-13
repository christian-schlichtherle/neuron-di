package org.neuron_di.api;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/** Enumerates strategies for caching the return values of methods. */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the annotated method:
     * Although not strictly required, a subsequent call by any thread should
     * return another instance.
     */
    DISABLED {

        @Override
        FixedValue decorate(FixedValue callback) { return callback; }

        @Override
        MethodInterceptor decorate(MethodInterceptor callback) {
            return callback;
        }
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
        FixedValue decorate(final FixedValue callback) {

            class Adapter extends NotThreadSafeStrategy<FixedValue>
                    implements FixedValueMixin {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new Adapter();
        }

        @Override
        MethodInterceptor decorate(final MethodInterceptor callback) {

            class Adapter extends NotThreadSafeStrategy<MethodInterceptor>
                    implements MethodInterceptorMixin {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new Adapter();
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
        FixedValue decorate(final FixedValue callback) {

            class Adapter extends ThreadLocalStrategy<FixedValue>
                    implements FixedValueMixin {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new Adapter();
        }

        @Override
        MethodInterceptor decorate(final MethodInterceptor callback) {

            class Adapter extends ThreadLocalStrategy<MethodInterceptor>
                    implements MethodInterceptorMixin {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new Adapter();
        }
    },

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by any thread must return the same instance.
     */
    THREAD_SAFE {

        @Override
        FixedValue decorate(final FixedValue callback) {

            class Adapter extends ThreadSafeStrategy<FixedValue>
                    implements FixedValueMixin {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new Adapter();
        }

        @Override
        MethodInterceptor decorate(final MethodInterceptor callback) {

            class Adapter extends ThreadSafeStrategy<MethodInterceptor>
                    implements MethodInterceptorMixin {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new Adapter();
        }
    };

    abstract FixedValue decorate(FixedValue callback);

    abstract MethodInterceptor decorate(MethodInterceptor callback);

    private abstract static class NotThreadSafeStrategy<C extends Callback>
            implements Strategy<C> {

        Object returnValue;

        @Override
        public <X extends Throwable> Object apply(final CallbackProxy<X> proxy) throws X {
            final Object value = returnValue;
            return null != value
                    ? value
                    : (returnValue = proxy.callback());
        }
    }

    private abstract static class ThreadLocalStrategy<C extends Callback>
            implements Strategy<C> {

        final ThreadLocal<Object> results = new ThreadLocal<>();

        @Override
        public <X extends Throwable> Object apply(final CallbackProxy<X> proxy) throws X {
            Object result = results.get();
            if (null == result) {
                results.set(result = proxy.callback());
            }
            return result;
        }
    }

    private abstract static class ThreadSafeStrategy<C extends Callback>
            implements Strategy<C> {

        volatile Object returnValue;

        @Override
        public <X extends Throwable> Object apply(final CallbackProxy<X> proxy) throws X {
            Object value;
            if (null == (value = returnValue)) {
                synchronized (this) {
                    if (null == (value = returnValue)) {
                        returnValue = value = proxy.callback();
                    }
                }
            }
            return value;
        }
    }

    private interface FixedValueMixin
            extends FixedValue, Strategy<FixedValue> {

        @Override
        default Object loadObject() throws Exception {
            return apply(() -> callback().loadObject());
        }
    }

    private interface MethodInterceptorMixin
            extends MethodInterceptor, Strategy<MethodInterceptor> {

        @Override
        default Object intercept(Object obj, Method method,
                                 Object[] args, MethodProxy proxy)
                throws Throwable {
            return apply(() -> callback().intercept(obj, method, args, proxy));
        }
    }

    private interface Strategy<C extends Callback>  {

        <X extends Throwable> Object apply(CallbackProxy<X> proxy) throws X;

        C callback();
    }

    private interface CallbackProxy<X extends Throwable> {

        Object callback() throws X;
    }
}
