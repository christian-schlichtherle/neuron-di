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

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.synchronizedMap;
import static java.util.Optional.*;

class Reflection {

    private static final MethodType acceptsNothingAndReturnsObject = methodType(Object.class);
    private static final MethodType acceptsObjectAndReturnsObject = methodType(Object.class, Object.class);

    private static final Lookup publicLookup = publicLookup();

    private static final Map<Class<?>, Map<String, MethodHandleMetaFactory>>
            classIndex = synchronizedMap(new WeakHashMap<>());

    private static volatile Map<Lookup, Map<MethodHandleMetaFactory, MethodHandleFactory>> lookupIndex;

    private static volatile Map<MethodHandleMetaFactory, MethodHandleFactory> publicLookupMethodHandleFactories;

    private Reflection() {
    }

    /**
     * Recursively searches for the named {@code member} in the given {@code object} and if found, returns a
     * corresponding {@link MethodHandle} created using the the given {@code lookup}.
     *
     * @throws BreedingException if the named {@code member} is not found in the given {@code object} or if the given
     *                           {@code lookup} has no access to it.
     */
    static MethodHandle methodHandle(final String member, final Object object, final Lookup lookup) {
        final Class<?> clazz = object.getClass();
        final MethodHandleMetaFactory mhmf = classIndex
                .computeIfAbsent(clazz, c -> new ConcurrentHashMap<>())
                .computeIfAbsent(member, m -> methodHandleMetaFactory(m, clazz));
        Map<MethodHandleMetaFactory, MethodHandleFactory> mhf;
        if (lookup.equals(publicLookup)) {
            if (null == (mhf = publicLookupMethodHandleFactories)) {
                synchronized (classIndex) {
                    if (null == (mhf = publicLookupMethodHandleFactories)) {
                        publicLookupMethodHandleFactories = mhf = synchronizedMap(new WeakHashMap<>());
                    }
                }
            }
        } else {
            Map<Lookup, Map<MethodHandleMetaFactory, MethodHandleFactory>> li;
            if (null == (li = lookupIndex)) {
                synchronized (classIndex) {
                    if (null == (li = lookupIndex)) {
                        lookupIndex = li = synchronizedMap(new WeakHashMap<>());
                    }
                }
            }
            mhf = li.computeIfAbsent(lookup, l -> new ConcurrentHashMap<>());
        }
        return mhf.computeIfAbsent(mhmf, mf -> mf.methodHandleFactory(lookup)).methodHandle(object);
    }

    private static MethodHandleMetaFactory methodHandleMetaFactory(String member, Class<?> clazz) {

        class MethodHandleMetaFactoryFinder implements Function<Class<?>, Optional<MethodHandleMetaFactory>> {

            private final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Optional<MethodHandleMetaFactory> apply(final Class<?> c) {
                Optional<Method> method;
                try {
                    final Method m = c.getDeclaredMethod(member);
                    if (isPublic(c) || isStatic(m)) {
                        return of(methodHandleMetaFactory(m, Reflection::unreflectMethod));
                    }
                    method = of(m);
                } catch (NoSuchMethodException e) {
                    try {
                        return of(methodHandleMetaFactory(c.getDeclaredField(member), Reflection::unreflectField));
                    } catch (NoSuchFieldException ignored) {
                    }
                    method = empty();
                }

                Optional<MethodHandleMetaFactory> superResult = ofNullable(c.getSuperclass()).flatMap(this);
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

                return method.map(m -> methodHandleMetaFactory(m, Reflection::unreflectMethod));
            }
        }

        return new MethodHandleMetaFactoryFinder()
                .apply(clazz)
                .orElseThrow(() ->
                        new BreedingException("A member named `" + member + "` neither exists in `" + clazz + "` nor in any of its superclasses and interfaces."));
    }

    private static <M extends AccessibleObject & Member>
    MethodHandleMetaFactory methodHandleMetaFactory(final M member, final Unreflect<M> unreflect) {
        member.setAccessible(true);
        if (isStatic(member)) {
            return lookup -> {
                final MethodHandle mh = unreflect.methodHandle(member, lookup).asType(acceptsNothingAndReturnsObject);
                return ignored -> mh;
            };
        } else {
            return lookup -> {
                final MethodHandle mh = unreflect.methodHandle(member, lookup).asType(acceptsObjectAndReturnsObject);
                return mh::bindTo;
            };
        }
    }

    private static MethodHandle unreflectMethod(final Method method, final Lookup lookup) {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
        }
    }

    private static MethodHandle unreflectField(final Field field, final Lookup lookup) {
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
        }
    }

    private static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    private interface Unreflect<M extends AccessibleObject & Member> {

        MethodHandle methodHandle(M member, Lookup lookup);
    }

    private interface MethodHandleMetaFactory {

        MethodHandleFactory methodHandleFactory(Lookup lookup);
    }

    private interface MethodHandleFactory {

        MethodHandle methodHandle(Object object);
    }
}
