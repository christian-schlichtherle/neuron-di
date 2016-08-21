package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

/** Mirrors {@link global.tranquillity.neuron.di.api.CachingStrategy}. */
enum RealCachingStrategy {

    /**
     * @see global.tranquillity.neuron.di.core.Organism#realCachingStrategy(global.tranquillity.neuron.di.api.CachingStrategy)
     */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        boolean isEnabled() { return false; }

        @Override
        FixedValue decorate(FixedValue callback) { return callback; }

        @Override
        NoOp decorate(MethodInterceptor callback) { return NoOp.INSTANCE; }
    },

    /**
     * @see global.tranquillity.neuron.di.core.Organism#realCachingStrategy(global.tranquillity.neuron.di.api.CachingStrategy)
     */
    @SuppressWarnings("unused")
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
     * @see global.tranquillity.neuron.di.core.Organism#realCachingStrategy(global.tranquillity.neuron.di.api.CachingStrategy)
     */
    @SuppressWarnings("unused")
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
    },

    /**
     * @see global.tranquillity.neuron.di.core.Organism#realCachingStrategy(global.tranquillity.neuron.di.api.CachingStrategy)
     */
    @SuppressWarnings("unused")
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
    };

    boolean isEnabled() { return true; }

    abstract Callback decorate(FixedValue callback);

    abstract Callback decorate(MethodInterceptor callback);

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
