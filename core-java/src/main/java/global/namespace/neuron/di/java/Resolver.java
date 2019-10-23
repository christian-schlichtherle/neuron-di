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

import global.namespace.neuron.di.internal.MethodInfo;
import global.namespace.neuron.di.internal.RealIncubator;

import java.util.*;

import static java.util.Optional.of;

class Resolver<T> {

    private static final Map<Class<?>, Object> fuzes = Collections.synchronizedMap(new WeakHashMap<>());

    private final Class<T> clazz;

    Resolver(final Class<T> clazz) {
        this.clazz = clazz;
    }

    <U> LinkedHashMap<MethodInfo, U> resolve(Map<DependencyResolver<T, ?>, U> bindings) {
        return new Object() {

            final LinkedHashMap<MethodInfo, U> resolved = new LinkedHashMap<>();
            int count = 0;

            {
                final T fuze = fuze();
                for (Map.Entry<DependencyResolver<T, ?>, U> binding : bindings.entrySet()) {
                    count++;
                    try {
                        binding.getKey().apply(fuze);
                        throw breedingException(null);
                    } catch (IgnitionError e) {
                        resolved.put(e.info(), binding.getValue());
                    } catch (BreedingException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw breedingException(e);
                    }
                }
            }

            BreedingException breedingException(Throwable cause) {
                return new BreedingException("Illegal binding: The parameter provided to the `bind` call at position " + count + " does not reference a synapse method.", cause);
            }
        }.resolved;
    }

    @SuppressWarnings("unchecked")
    private T fuze() {
        return (T) fuzes.computeIfAbsent(clazz, c -> RealIncubator.breed(c, Resolver::ignition));
    }

    private static Optional<DependencyProvider<?>> ignition(MethodInfo info) {
        return of(() -> {
            throw new IgnitionError(info);
        });
    }

    private static final class IgnitionError extends Error {

        private static final long serialVersionUID = 0L;

        private final MethodInfo info;

        IgnitionError(final MethodInfo info) {
            super(null, null, false, false);
            this.info = info;
        }

        MethodInfo info() {
            return info;
        }
    }
}
