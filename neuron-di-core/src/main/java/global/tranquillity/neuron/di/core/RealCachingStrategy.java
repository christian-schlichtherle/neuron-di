package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.CachingStrategy;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

/** Mirrors {@link global.tranquillity.neuron.di.api.CachingStrategy}. */
enum RealCachingStrategy {

    /** @see #valueOf(global.tranquillity.neuron.di.api.CachingStrategy) */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        Callback synapseCallback(FixedValue callback) { return callback; }

        @Override
        Callback methodCallback() { return NoOp.INSTANCE; }
    },

    /** @see #valueOf(global.tranquillity.neuron.di.api.CachingStrategy) */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {

        @Override
        Callback synapseCallback(final FixedValue callback) {

            class SynapseCallback
                    extends NotThreadSafeCache
                    implements SynapseCache {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodCallback
                    extends NotThreadSafeCache
                    implements MethodCache {
            }

            return new MethodCallback();
        }
    },

    /** @see #valueOf(global.tranquillity.neuron.di.api.CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_SAFE {

        @Override
        Callback synapseCallback(final FixedValue callback) {

            class SynapseCallback
                    extends ThreadSafeCache
                    implements SynapseCache {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodCallback
                    extends ThreadSafeCache
                    implements MethodCache {
            }

            return new MethodCallback();
        }
    },

    /** @see #valueOf(global.tranquillity.neuron.di.api.CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_LOCAL {

        @Override
        Callback synapseCallback(final FixedValue callback) {

            class SynapseCallback
                    extends ThreadLocalCache
                    implements SynapseCache {

                @Override
                public FixedValue callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodCallback
                    extends ThreadLocalCache
                    implements MethodCache {
            }

            return new MethodCallback();
        }
    };

    private static final MethodInterceptor invokeSuper =
            (obj, method, args, proxy) -> proxy.invokeSuper(obj, args);

    static RealCachingStrategy valueOf(CachingStrategy strategy) {
        return valueOf(strategy.name());
    }

    abstract Callback synapseCallback(FixedValue callback);

    abstract Callback methodCallback();

    private static class NotThreadSafeCache implements Cache {

        Object returnValue;

        @Override
        public <X extends Throwable> Object apply(final Proxy<X> proxy) throws X {
            final Object value = returnValue;
            return null != value ? value : (returnValue = proxy.get());
        }
    }

    private static class ThreadSafeCache implements Cache {

        volatile Object returnValue;

        @Override
        public <X extends Throwable> Object apply(final Proxy<X> proxy) throws X {
            Object value;
            if (null == (value = returnValue)) {
                synchronized (this) {
                    if (null == (value = returnValue)) {
                        returnValue = value = proxy.get();
                    }
                }
            }
            return value;
        }
    }

    private static class ThreadLocalCache implements Cache {

        final ThreadLocal<Object> results = new ThreadLocal<>();

        @Override
        public <X extends Throwable> Object apply(final Proxy<X> proxy) throws X {
            Object result = results.get();
            if (null == result) {
                results.set(result = proxy.get());
            }
            return result;
        }
    }

    private interface SynapseCache
            extends FixedValue, CallbackCache<FixedValue> {

        @Override
        default Object loadObject() throws Exception {
            return apply(() -> callback().loadObject());
        }
    }

    private interface MethodCache
            extends MethodInterceptor, CallbackCache<MethodInterceptor> {

        @Override
        default Object intercept(Object obj, Method method,
                                 Object[] args, MethodProxy proxy)
                throws Throwable {
            return apply(() -> callback().intercept(obj, method, args, proxy));
        }

        @Override
        default MethodInterceptor callback() { return invokeSuper; }
    }

    private interface CallbackCache<C extends Callback> extends Cache {

        C callback();
    }

    private interface Cache {

        <X extends Throwable> Object apply(Proxy<X> proxy) throws X;
    }

    private interface Proxy<X extends Throwable> {

        Object get() throws X;
    }
}
