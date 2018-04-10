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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

/** @author Christian Schlichtherle */
class Reflection {

    private Reflection() { }

    /**
     * Returns a consumer which applies the given consumer to all elements of the type hierarchy represented by its
     * class parameter.
     * The traversal starts with calling the given consumer for the given type, then applies itself recursively to all
     * of the interfaces implemented by the given type (if any) and finally to the superclass of the given type (if
     * existing).
     * Note that due to interfaces, the type hierarchy can be a graph.
     * The returned function will visit any interface at most once, however.
     */
    static Consumer<Class<?>> traverse(final Consumer<Class<?>> consumer) {
        return clazz -> {
            new Consumer<Class<?>>() {

                final Set<Class<?>> interfaces = new HashSet<>();

                @Override
                public void accept(final Class<?> visitor) {
                    consumer.accept(visitor);
                    for (final Class<?> iface : visitor.getInterfaces()) {
                        if (!interfaces.contains(iface)) {
                            accept(iface);
                            interfaces.add(iface);
                        }
                    }
                    final Class<?> zuper = visitor.getSuperclass();
                    if (null != zuper) {
                        accept(zuper);
                    }
                }
            }.accept(clazz);
        };
    }

    @SuppressWarnings({"unchecked", "Since15"})
    static <C> Class<? extends C> defineSubclass(final Class<C> clazz, final String name, final byte[] b) {
        try {
            return (Class<? extends C>) privateLookupIn(clazz, lookup()).defineClass(b);
        } catch (NoSuchMethodError e) {
            return Reflection8.defineSubclass(clazz, name, b);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static Class<?> boxed(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == Boolean.TYPE) {
                return Boolean.class;
            } else if (clazz == Byte.TYPE) {
                return Byte.class;
            } else if (clazz == Character.TYPE) {
                return Character.class;
            } else if (clazz == Double.TYPE) {
                return Double.class;
            } else if (clazz == Float.TYPE) {
                return Float.class;
            } else if (clazz == Integer.TYPE) {
                return Integer.class;
            } else if (clazz == Long.TYPE) {
                return Long.class;
            } else if (clazz == Short.TYPE) {
                return Short.class;
            } else {
                assert clazz == Void.TYPE;
                return Void.class;
            }
        } else {
            return clazz;
        }
    }
}
