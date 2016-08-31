/**
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
package global.namespace.neuron.di.api;

/** Enumerates strategies for caching the return values of methods. */
public enum CachingStrategy {

    /**
     * Does not cache the return value of the annotated method:
     * Although not strictly required, a subsequent call by any thread should
     * return another instance.
     */
    DISABLED {

        @Override
        public boolean isEnabled() { return false; }
    },

    /**
     * Caches the return value of the annotated method:
     * Although not strictly required, a subsequent call by the same thread
     * should return the same instance.
     * A subsequent call by any other thread may return another instance.
     * This definition recursively applies to any thread.
     */
    NOT_THREAD_SAFE,

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by any thread must return the same instance.
     */
    THREAD_SAFE,

    /**
     * Caches the return value of the annotated method:
     * A subsequent call by the same thread must return the same instance.
     * Although not strictly required, a subsequent call by any other thread
     * should return another instance.
     * This definition recursively applies to any thread.
     */
    THREAD_LOCAL;

    /** Returns true iff the caching strategy is not {@link #DISABLED}. */
    public boolean isEnabled() { return true; }
}
