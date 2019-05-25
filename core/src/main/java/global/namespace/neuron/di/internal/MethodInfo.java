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

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.CachingStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static global.namespace.neuron.di.internal.Reflection.findAnnotation;

@FunctionalInterface
public interface MethodInfo {

    default boolean canEqual(Object other) {
        return other instanceof MethodInfo;
    }

    default Optional<Caching> findCachingAnnotation() {
        return findAnnotation(Caching.class).apply(method());
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

    Method method();

    default Optional<CachingStrategy> methodCachingStrategy() {
        return findCachingAnnotation().map(Caching::value);
    }

    default String name() {
        return method().getName();
    }

    default Class<?> returnType() {
        return method().getReturnType();
    }
}
