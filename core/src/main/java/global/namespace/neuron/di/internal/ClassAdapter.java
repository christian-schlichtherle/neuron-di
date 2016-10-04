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

/** Adapts a function which accepts a class object reflecting a super class or interface. */
final class ClassAdapter<T, U> implements Function<Class<T>, U> {

    private final Function<Class<? extends T>, U> function;

    /** @param function a function which accepts a class object reflecting a super class or interface. */
    ClassAdapter(final Function<Class<? extends T>, U> function) { this.function = function; }

    /** Calls the adapted function and returns its value. */
    @Override
    public U apply(final Class<T> runtimeClass) {
        final Class<? extends T> superclass;
        if (runtimeClass.isInterface()) {
            final Optional<Class<? extends T>> lookup = lookupScalaCompanion(runtimeClass);
            if (lookup.isPresent()) {
                superclass = lookup.get();
            } else {
                superclass = runtimeClass;
            }
        } else {
            superclass = runtimeClass;
        }
        return function.apply(superclass);
    }

    @SuppressWarnings("unchecked")
    private Optional<Class<? extends T>> lookupScalaCompanion(final Class<T> runtimeClass) {
        try {
            return Optional.of((Class<? extends T>) associatedClassLoader(runtimeClass)
                    .loadClass(runtimeClass.getName() + "$$shim"));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
