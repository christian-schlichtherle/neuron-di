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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static global.namespace.neuron.di.internal.Reflection.associatedClassLoader;

/**
 * Adapts a consumer which accepts a class object reflecting a super class
 * and an array of class objects reflecting interfaces to a consumer which
 * accepts a class object reflecting a class or interface.
 */
final class ClassAdapter<T> implements Function<Class<?>, T> {

    private static Class<?>[] NO_CLASSES = new Class<?>[0];

    private final BiFunction<Class<?>, Class<?>[], T> function;

    /**
     * @param function a consumer which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     */
    ClassAdapter(final BiFunction<Class<?>, Class<?>[], T> function) { this.function = function; }

    /** Calls the adapted consumer and returns its value. */
    @Override
    public T apply(final Class<?> runtimeClass) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (runtimeClass.isInterface()) {
            final Optional<Class<?>> lookup = lookupScalaCompanion(runtimeClass);
            if (lookup.isPresent()) {
                superclass = lookup.get();
                interfaces = NO_CLASSES;
            } else {
                superclass = Object.class;
                interfaces = new Class<?>[] { runtimeClass };
            }
        } else {
            superclass = runtimeClass;
            interfaces = NO_CLASSES;
        }
        return function.apply(superclass, interfaces);
    }

    private Optional<Class<?>> lookupScalaCompanion(final Class<?> runtimeClass) {
        try {
            return Optional.of(associatedClassLoader(runtimeClass)
                    .loadClass(runtimeClass.getName() + "$$shim"));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
