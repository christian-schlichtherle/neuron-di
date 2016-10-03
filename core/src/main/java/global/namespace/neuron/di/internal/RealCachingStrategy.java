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
package global.namespace.neuron.di.internal;

import global.namespace.neuron.di.java.CachingStrategy;

/** Mirrors {@link CachingStrategy}. */
enum RealCachingStrategy {

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        <T, X extends Throwable> MethodProxy<T, X> decorate(MethodProxy<T, X> methodProxy) { return methodProxy; }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {

        @Override
        <T, X extends Throwable> MethodProxy<T, X> decorate(final MethodProxy<T, X> methodProxy) {
            return new MethodProxy<T, X>() {

                T returnValue;

                @Override
                public T get() throws X {
                    final T value = returnValue;
                    return null != value ? value : (returnValue = methodProxy.get());
                }
            };
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_SAFE {

        @Override
        <T, X extends Throwable> MethodProxy<T, X> decorate(final MethodProxy<T, X> methodProxy) {
            return new MethodProxy<T, X>() {

                volatile T returnValue;

                @Override
                public T get() throws X {
                    T value;
                    if (null == (value = returnValue)) {
                        synchronized (this) {
                            if (null == (value = returnValue)) {
                                returnValue = value = methodProxy.get();
                            }
                        }
                    }
                    return value;
                }
            };
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_LOCAL {

        @Override
        <T, X extends Throwable> MethodProxy<T, X> decorate(final MethodProxy<T, X> methodProxy) {
            return new MethodProxy<T, X>() {

                final ThreadLocal<T> results = new ThreadLocal<>();

                @Override
                public T get() throws X {
                    T result = results.get();
                    if (null == result) {
                        results.set(result = methodProxy.get());
                    }
                    return result;
                }
            };
        }
    };

    static RealCachingStrategy valueOf(CachingStrategy strategy) { return valueOf(strategy.name()); }

    abstract <T, X extends Throwable> MethodProxy<T, X> decorate(MethodProxy<T, X> methodProxy);
}
