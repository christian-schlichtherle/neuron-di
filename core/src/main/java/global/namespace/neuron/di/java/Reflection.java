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
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.synchronizedMap;
import static java.util.Optional.*;

class Reflection {

    private static final Map<Class<?>, Map<String, MethodHandleFactory>>
            classIndex = synchronizedMap(new WeakHashMap<>());

    private static final MethodType acceptsNothingAndReturnsObject = methodType(Object.class);
    private static final MethodType acceptsObjectAndReturnsObject = methodType(Object.class, Object.class);

    private Reflection() {
    }

    /**
     * Searches for the named {@code member} in the given {@code object} using the the given {@code lookup} and returns
     * a corresponding {@link MethodHandle} if found.
     *
     * @throws BreedingException if the named {@code member} is not found in the given {@code object} or if the given
     *                           {@code lookup} has no access to it.
     */
    static MethodHandle methodHandle(final String member, final Object object, final Lookup lookup) {
        final Class<?> clazz = object.getClass();
        return classIndex
                .computeIfAbsent(clazz, c -> new ConcurrentHashMap<>())
                .computeIfAbsent(member, m -> methodHandleFactory(m, clazz))
                .methodHandle(object, lookup);
    }

    private static MethodHandleFactory methodHandleFactory(String member, Class<?> clazz) {

        class MethodHandleFactoryFinder implements Function<Class<?>, Optional<MethodHandleFactory>> {

            private final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Optional<MethodHandleFactory> apply(final Class<?> c) {
                Optional<Method> method;
                try {
                    final Method m = c.getDeclaredMethod(member);
                    if (isPublic(c) || isStatic(m)) {
                        return of(methodHandleFactory(m, Reflection::unreflectMethod));
                    }
                    method = of(m);
                } catch (NoSuchMethodException e) {
                    try {
                        return of(methodHandleFactory(c.getDeclaredField(member), Reflection::unreflectField));
                    } catch (NoSuchFieldException ignored) {
                    }
                    method = empty();
                }

                Optional<MethodHandleFactory> superResult = ofNullable(c.getSuperclass()).flatMap(this);
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

                return method.map(m -> methodHandleFactory(m, Reflection::unreflectMethod));
            }
        }

        return new MethodHandleFactoryFinder()
                .apply(clazz)
                .orElseThrow(() ->
                        new BreedingException("A member named `" + member + "` neither exists in `" + clazz + "` nor in any of its superclasses and interfaces."));
    }

    private static MethodHandle unreflectMethod(final Method method, final Lookup lookup) {
        try {
            return modify(method, lookup.unreflect(method));
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
        }
    }

    private static MethodHandle unreflectField(final Field field, final Lookup lookup) {
        try {
            return modify(field, lookup.unreflectGetter(field));
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
        }
    }

    private static <M extends AccessibleObject & Member>
    MethodHandle modify(M member, MethodHandle mh) {
        return isStatic(member) ? mh.asType(acceptsNothingAndReturnsObject) : mh.asType(acceptsObjectAndReturnsObject);
    }

    private static <M extends AccessibleObject & Member>
    MethodHandleFactory methodHandleFactory(final M member, final Unreflect<M> unreflect) {
        member.setAccessible(true);
        if (isStatic(member)) {
            return new MethodHandleFactory() {

                @Override
                MethodHandle methodHandle(Object ignored, Lookup lookup) {
                    return methodHandle(member, unreflect, lookup);
                }
            };
        } else {
            return new MethodHandleFactory() {

                @Override
                MethodHandle methodHandle(Object object, Lookup lookup) {
                    return methodHandle(member, unreflect, lookup).bindTo(object);
                }
            };
        }
    }

    private static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    private interface Unreflect<M extends AccessibleObject & Member> {

        MethodHandle apply(M member, Lookup lookup);
    }

    private static abstract class MethodHandleFactory {

        private final Map<Lookup, MethodHandle> methodHandles = synchronizedMap(new WeakHashMap<>());

        abstract MethodHandle methodHandle(Object object, Lookup lookup);

        <M extends AccessibleObject & Member>
        MethodHandle methodHandle(M member, Unreflect<M> unreflect, Lookup lookup) {
            return methodHandles.computeIfAbsent(lookup, l -> unreflect.apply(member, l));
        }
    }
}
