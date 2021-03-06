/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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

/**
 * Mirrors {@link CachingStrategy}.
 */
enum RealCachingStrategy {

    /**
     * @see #valueOf(CachingStrategy)
     */
    @SuppressWarnings("unused")
    DISABLED {
        @Override
        <D> DependencyProvider<D> decorate(DependencyProvider<D> provider) {
            return provider;
        }
    },

    /**
     * @see #valueOf(CachingStrategy)
     */
    @SuppressWarnings("unused")
    NOT_THREAD_SAFE {
        @Override
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                boolean init;
                D value;

                @Override
                public D get() throws Throwable {
                    if (!init) {
                        value = provider.get();
                        init = true;
                    }
                    return value;
                }
            };
        }
    },

    /**
     * @see #valueOf(CachingStrategy)
     */
    @SuppressWarnings("unused")
    THREAD_SAFE {
        @Override
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                volatile boolean init;
                volatile D value;

                @Override
                public D get() throws Throwable {
                    if (!init) {
                        synchronized (this) {
                            if (!init) {
                                value = provider.get();
                                init = true;
                            }
                        }
                    }
                    return value;
                }
            };
        }
    },

    /**
     * @see #valueOf(CachingStrategy)
     */
    @SuppressWarnings("unused")
    THREAD_LOCAL {
        @Override
        <D> DependencyProvider<D> decorate(final DependencyProvider<D> provider) {
            return new DependencyProvider<D>() {

                final ThreadLocal<DependencyProvider<D>> results = new ThreadLocal<>();

                @Override
                public D get() throws Throwable {
                    DependencyProvider<D> result = results.get();
                    if (null == result) {
                        final D value = provider.get(); // must resolve now or else there is no caching at all!
                        results.set(result = () -> value);
                    }
                    return result.get();
                }
            };
        }
    };

    static RealCachingStrategy valueOf(CachingStrategy strategy) {
        return valueOf(strategy.name());
    }

    abstract <D> DependencyProvider<D> decorate(DependencyProvider<D> provider);
}
