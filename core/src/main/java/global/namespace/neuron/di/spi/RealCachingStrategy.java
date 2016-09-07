/*
 * Copyright © 2016 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package global.namespace.neuron.di.spi;

import global.namespace.neuron.di.api.CachingStrategy;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

/** Mirrors {@link CachingStrategy}. */
enum RealCachingStrategy {

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        Callback synapseCallback(MethodInterceptor callback) {
            return callback;
        }

        @Override
        Callback methodCallback() { return NoOp.INSTANCE; }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {

        @Override
        Callback synapseCallback(final MethodInterceptor callback) {

            class SynapseCallback
                    extends NotThreadSafeCache
                    implements MethodInterceptorCache {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodInterceptorCallback
                    extends NotThreadSafeCache
                    implements MethodInterceptorCache {
            }

            return new MethodInterceptorCallback();
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_SAFE {

        @Override
        Callback synapseCallback(final MethodInterceptor callback) {

            class SynapseCallback
                    extends ThreadSafeCache
                    implements MethodInterceptorCache {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodInterceptorCallback
                    extends ThreadSafeCache
                    implements MethodInterceptorCache {
            }

            return new MethodInterceptorCallback();
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_LOCAL {

        @Override
        Callback synapseCallback(final MethodInterceptor callback) {

            class SynapseCallback
                    extends ThreadLocalCache
                    implements MethodInterceptorCache {

                @Override
                public MethodInterceptor callback() { return callback; }
            }

            return new SynapseCallback();
        }

        @Override
        Callback methodCallback() {

            class MethodInterceptorCallback
                    extends ThreadLocalCache
                    implements MethodInterceptorCache {
            }

            return new MethodInterceptorCallback();
        }
    };

    private static final MethodInterceptor invokeSuper =
            (obj, method, args, proxy) -> proxy.invokeSuper(obj, args);

    static RealCachingStrategy valueOf(CachingStrategy strategy) {
        return valueOf(strategy.name());
    }

    abstract Callback synapseCallback(MethodInterceptor callback);

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

    private interface MethodInterceptorCache
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
