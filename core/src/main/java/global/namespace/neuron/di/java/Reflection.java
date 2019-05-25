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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

class Reflection {

    private static final MethodType objectMethodType = methodType(Object.class);

    private Reflection() {
    }

    static Find find(final String member) {
        return new Find() {

            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Optional<MethodHandle> in(final Object target) {
                return new Function<Class<?>, Optional<MethodHandle>>() {
                    @Override
                    public Optional<MethodHandle> apply(final Class<?> c) {
                        final MethodHandles.Lookup lookup = publicLookup();
                        try {
                            return methodHandle(c.getDeclaredMethod(member), lookup::unreflect);
                        } catch (final NoSuchMethodException ignored) {
                            try {
                                return methodHandle(c.getDeclaredField(member), lookup::unreflectGetter);
                            } catch (final NoSuchFieldException ignoredAgain) {
                                Optional<MethodHandle> result;
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
                                return Optional.empty();
                            }
                        }
                    }

                    <M extends AccessibleObject & Member>
                    Optional<MethodHandle> methodHandle(M member, Unreflect<M> unreflect) {
                        member.setAccessible(true);
                        MethodHandle mh;
                        try {
                            mh = unreflect.apply(member);
                        } catch (IllegalAccessException e) {
                            throw new AssertionError(e);
                        }
                        if (0 == (member.getModifiers() & Modifier.STATIC)) {
                            mh = mh.bindTo(target);
                        }
                        return Optional.of(mh.asType(objectMethodType));
                    }
                }.apply(target.getClass());
            }
        };
    }

    private interface Unreflect<M> {

        MethodHandle apply(M member) throws IllegalAccessException;
    }

    interface Find {

        Optional<MethodHandle> in(Object target);
    }
}
