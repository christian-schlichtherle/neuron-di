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
package global.namespace.neuron.di.java;

/**
 * Resolves some dependency of a given neuron.
 *
 * @param <N> the type of the neuron.
 * @param <D> the type of the dependency.
 */
@FunctionalInterface
public interface DependencyResolver<N, D> {

    /**
     * Returns the dependency of the given neuron.
     *
     * @param neuron the neuron.
     */
    D apply(N neuron) throws Throwable;
}