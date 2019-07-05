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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Optional.*;

class Reflection {

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
                .computeIfAbsent(member, m -> findMethodHandle0(clazz, m))
                .map(f -> f.apply(object));
    }

    private static Optional<Function<Object, MethodHandle>> findMethodHandle0(final Class<?> clazz, final String member) {
        return new Function<Class<?>, Optional<Function<Object, MethodHandle>>>() {

            final Lookup lookup = publicLookup();
            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Optional<Function<Object, MethodHandle>> apply(final Class<?> c) {
                Optional<Method> method;
                try {
                    final Method m = c.getDeclaredMethod(member);
                    if (isPublic(c) || isStatic(m)) {
                        return of(methodHandle(m, lookup::unreflect));
                    }
                    method = of(m);
                } catch (NoSuchMethodException e) {
                    try {
                        return of(methodHandle(c.getDeclaredField(member), lookup::unreflectGetter));
                    } catch (NoSuchFieldException ignored) {
                    }
                    method = empty();
                }

                Optional<Function<Object, MethodHandle>> superResult = ofNullable(c.getSuperclass()).flatMap(this);
                if (superResult.isPresent()) {
                    return superResult;
                }
                for (final Class<?> iface : c.getInterfaces()) {
                    if (!interfaces.contains(iface)) {
                        if ((superResult = apply(iface)).isPresent()) {
                            return superResult;
                        }
                        interfaces.add(iface);
                    }
                }

                return method.map(m -> methodHandle(m, lookup::unreflect));
            }
        }.apply(clazz);
    }

    private static <M extends AccessibleObject & Member> Function<Object, MethodHandle> methodHandle(
            final M member,
            final Unreflect<M> unreflect
    ) {
        member.setAccessible(true);
        try {
            final MethodHandle mh;
            if (isStatic(member)) {
                mh = unreflect.apply(member).asType(acceptsNothingAndReturnsObject);
                return ignored -> mh;
            } else {
                mh = unreflect.apply(member).asType(acceptsObjectAndReturnsObject);
                return mh::bindTo;
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    private interface Unreflect<M> {

        MethodHandle apply(M member) throws IllegalAccessException;
    }
}
