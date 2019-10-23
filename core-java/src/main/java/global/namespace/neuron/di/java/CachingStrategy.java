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
package global.namespace.neuron.di.java;

/**
 * Enumerates strategies for caching the return values of synapse methods.
 */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the synapse method.
     * <p>
     * Suppose a synapse method without a {@link Caching} annotation returns a different instance on each call, that is,
     * it behaves like a factory.
     * Now if this strategy is applied instead and {@code n} threads each make {@code m} calls to this synapse method,
     * then exactly {@code n * m} different instances are returned.
     */
    DISABLED {
        @Override
        public boolean isEnabled() {
            return false;
        }
    },

    /**
     * Caches the return value of the synapse method so that it's <em>not</em> thread-safe.
     * <p>
     * Suppose a synapse method without a {@link Caching} annotation returns a different instance on each call, that is,
     * it behaves like a factory.
     * Now if this strategy is applied instead and {@code n} threads each make {@code m} calls to this synapse method,
     * then at most {@code n} different instances are returned.
     */
    NOT_THREAD_SAFE,

    /**
     * Caches the return value of the synapse method so that it's thread-safe.
     * <p>
     * Suppose a synapse method without a {@link Caching} annotation returns a different instance on each call, that is,
     * it behaves like a factory.
     * Now if this strategy is applied instead and {@code n} threads each make {@code m} calls to this synapse method,
     * then exactly one instance is returned.
     */
    THREAD_SAFE,

    /**
     * Caches the return value of the synapse method so that it's thread local.
     * <p>
     * Suppose a synapse method without a {@link Caching} annotation returns a different instance on each call, that is,
     * it behaves like a factory.
     * Now if this strategy is applied instead and {@code n} threads each make {@code m} calls to this synapse method,
     * then exactly {@code n} different instances are returned.
     */
    THREAD_LOCAL;

    /**
     * Returns true iff the caching strategy is not {@link #DISABLED}.
     */
    public boolean isEnabled() {
        return true;
    }
}
