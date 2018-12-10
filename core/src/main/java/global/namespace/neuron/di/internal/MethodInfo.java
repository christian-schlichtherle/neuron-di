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

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.CachingStrategy;

import java.lang.reflect.*;
import java.util.Optional;

import static io.leangen.geantyref.GenericTypeReflector.getExactReturnType;
import static java.util.Optional.ofNullable;

@FunctionalInterface
public interface MethodInfo {

    Method method();

    default Optional<CachingStrategy> methodCachingStrategy() {
        return ofNullable(method().getDeclaredAnnotation(Caching.class)).map(Caching::value);
    }

    default boolean hasParameters() {
        return 0 != method().getParameterCount();
    }

    default boolean isAbstract() {
        return Modifier.isAbstract(method().getModifiers());
    }

    default boolean isVoid() {
        final Class<?> returnType = method().getReturnType();
        return Void.TYPE == returnType || Void.class == returnType;
    }

    default String name() {
        return method().getName();
    }

    default Class<?> inferReturnTypeIn(Class<?> clazz) {
        Type type = getExactReturnType(method(), clazz);
        while (!(type instanceof Class<?>)) {
            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
            } else if (type instanceof WildcardType) {
                type = ((WildcardType) type).getUpperBounds()[0];
            } else {
                type = method().getReturnType();
            }
        }
        return (Class<?>) type;
    }

    default boolean canEqual(Object other) {
        return other instanceof MethodInfo;
    }
}
