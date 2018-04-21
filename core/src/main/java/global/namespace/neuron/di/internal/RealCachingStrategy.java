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
import global.namespace.neuron.di.java.DependencyProvider;

/** Mirrors {@link CachingStrategy}. */
enum RealCachingStrategy {

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    DISABLED {

        @Override
        <D> DependencyProvider<D> decorate(DependencyProvider<D> provider) { return provider; }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {

        @Override
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                D returnValue;

                @Override
                public D get() throws Exception {
                    final D value = returnValue;
                    return null != value ? value : (returnValue = provider.get());
                }
            };
        }
    },

    /** @see #valueOf(CachingStrategy) */
    @SuppressWarnings("unused")
    THREAD_SAFE {

        @Override
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                volatile D returnValue;

                @Override
                public D get() throws Exception {
                    D value;
                    if (null == (value = returnValue)) {
                        synchronized (this) {
                            if (null == (value = returnValue)) {
                                returnValue = value = provider.get();
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
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                final ThreadLocal<D> results = new ThreadLocal<>();

                @Override
                public D get() throws Exception {
                    D result = results.get();
                    if (null == result) {
                        results.set(result = provider.get());
                    }
                    return result;
                }
            };
        }
    };

    static RealCachingStrategy valueOf(CachingStrategy strategy) { return valueOf(strategy.name()); }

    abstract <D> DependencyProvider<D> decorate(DependencyProvider<D> provider);
}
