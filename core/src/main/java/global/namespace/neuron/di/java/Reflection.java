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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Optional.empty;
import static java.util.Optional.of;

final class Reflection {

    private static final Map<Class<?>, Map<String, Optional<Function<Object, MethodHandle>>>> classIndex =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final MethodType acceptsNothingAndReturnsObject = methodType(Object.class);
    private static final MethodType acceptsObjectAndReturnsObject = methodType(Object.class, Object.class);

    private Reflection() {
    }

    static Optional<MethodHandle> findMethodHandle(final Object object, final String member) {
        final Class<?> clazz = object.getClass();
        return classIndex
                .computeIfAbsent(clazz, c -> new ConcurrentHashMap<>())
                .computeIfAbsent(member, m -> find0(clazz, m))
                .map(f -> f.apply(object));
    }

    private static Optional<Function<Object, MethodHandle>> find0(final Class<?> clazz, final String member) {
        return new Function<Class<?>, Optional<Function<Object, MethodHandle>>>() {

            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Optional<Function<Object, MethodHandle>> apply(final Class<?> c) {
                final MethodHandles.Lookup lookup = publicLookup();
                try {
                    return methodHandle(c.getDeclaredMethod(member), lookup::unreflect);
                } catch (final NoSuchMethodException ignored) {
                    try {
                        return methodHandle(c.getDeclaredField(member), lookup::unreflectGetter);
                    } catch (final NoSuchFieldException ignoredAgain) {
                        Optional<Function<Object, MethodHandle>> result;
                        for (final Class<?> iface : c.getInterfaces()) {
                            if (!interfaces.contains(iface)) {
                                if ((result = apply(iface)).isPresent()) {
                                    return result;
                                }
                                interfaces.add(iface);
                            }
                        }
                        final Class<?> zuper = c.getSuperclass();
                        if (null != zuper) {
                            if ((result = apply(zuper)).isPresent()) {
                                return result;
                            }
                        }
                        return empty();
                    }
                }
            }

            <M extends AccessibleObject & Member>
            Optional<Function<Object, MethodHandle>> methodHandle(final M member, final Unreflect<M> unreflect) {
                member.setAccessible(true);
                try {
                    final MethodHandle mh;
                    if (0 == (member.getModifiers() & Modifier.STATIC)) {
                        mh = unreflect.apply(member).asType(acceptsObjectAndReturnsObject);
                        return of(mh::bindTo);
                    } else {
                        mh = unreflect.apply(member).asType(acceptsNothingAndReturnsObject);
                        return of(ignored -> mh);
                    }
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }.apply(clazz);
    }

    private interface Unreflect<M> {

        MethodHandle apply(M member) throws IllegalAccessException;
    }
}
