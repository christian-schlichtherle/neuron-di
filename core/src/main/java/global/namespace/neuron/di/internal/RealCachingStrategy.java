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
import global.namespace.neuron.di.java.DependencySupplier;

/** Mirrors {@link CachingStrategy}. */
enum RealCachingStrategy {

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        <T> DependencySupplier<T> decorate(DependencySupplier<T> supplier) { return supplier; }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {

        @Override
        <T> DependencySupplier<T> decorate(final DependencySupplier<T> supplier) {
            return new DependencySupplier<T>() {

                T returnValue;

                @Override
                public T get() throws Throwable {
                    final T value = returnValue;
                    return null != value ? value : (returnValue = supplier.get());
                }
            };
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_SAFE {

        @Override
        <T> DependencySupplier<T> decorate(final DependencySupplier<T> supplier) {
            return new DependencySupplier<T>() {

                volatile T returnValue;

                @Override
                public T get() throws Throwable {
                    T value;
                    if (null == (value = returnValue)) {
                        synchronized (this) {
                            if (null == (value = returnValue)) {
                                returnValue = value = supplier.get();
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
        <T> DependencySupplier<T> decorate(final DependencySupplier<T> supplier) {
            return new DependencySupplier<T>() {

                final ThreadLocal<T> results = new ThreadLocal<>();

                @Override
                public T get() throws Throwable {
                    T result = results.get();
                    if (null == result) {
                        results.set(result = supplier.get());
                    }
                    return result;
                }
            };
        }
    };

    static RealCachingStrategy valueOf(CachingStrategy strategy) { return valueOf(strategy.name()); }

    abstract <T> DependencySupplier<T> decorate(DependencySupplier<T> supplier);
}
