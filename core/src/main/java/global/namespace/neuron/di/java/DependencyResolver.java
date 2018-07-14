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
 * Resolves some dependency of an object.
 *
 * @param <T> the type of the object.
 * @param <D> the type of the dependency.
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface DependencyResolver<T, D> {

    /**
     * Returns the dependency of the object.
     *
     * @param object the object.
     */
    D apply(T object) throws Throwable;
}
