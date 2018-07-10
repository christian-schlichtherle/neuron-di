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

import global.namespace.neuron.di.internal.RealIncubator;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Optional.of;

final class Resolver {

    private static final Map<Class<?>, Object> fuzes = Collections.synchronizedMap(new WeakHashMap<>());

    private Resolver() { }

    static <T> Map<Method, DependencyResolver<? super T, ?>> resolve(Class<T> clazz, List<Map.Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>>> bindings) {
        return new Object() {

            final T fuze = fuze(clazz);
            int currentPosition = 0;

            Map<Method, DependencyResolver<? super T, ?>> apply() {
                final Map<Method, DependencyResolver<? super T, ?>> resolvers = new LinkedHashMap<>();
                for (Map.Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>> binding : bindings) {
                    currentPosition++;
                    try {
                        binding.getKey().apply(fuze);
                        throw breedingException(null);
                    } catch (IgnitionError e) {
                        resolvers.put(e.method(), binding.getValue());
                    } catch (BreedingException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw breedingException(e);
                    }
                }
                return resolvers;
            }

            BreedingException breedingException(Throwable cause) {
                return new BreedingException("Illegal binding: The parameter provided to the `bind` call at position " + currentPosition + " does not reference a synapse method.", cause);
            }
        }.apply();
    }

    @SuppressWarnings("unchecked")
    private static <T> T fuze(Class<T> clazz) {
        return (T) fuzes.computeIfAbsent(clazz, c -> Incubator.breed(c, Resolver::blowUp));
    }

    private static Optional<DependencyProvider<?>> blowUp(Method method) {
        return of(() -> { throw new IgnitionError(method); });
    }

    private static final class IgnitionError extends Error {

        private static final long serialVersionUID = 0L;

        private final Method method;

        IgnitionError(final Method method) {
            super(null, null, false, false);
            this.method = method;
        }

        Method method() { return method; }
    }
}
