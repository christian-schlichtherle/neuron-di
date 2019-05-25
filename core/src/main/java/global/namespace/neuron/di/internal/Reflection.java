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

import global.namespace.neuron.di.internal.proxy.Proxies;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

class Reflection {

    // VOLATILE methods are bridge methods, e.g. for use in generic classes.
    private static final int PRIVATE_STATIC_VOLATILE = PRIVATE | STATIC | VOLATILE;

    private static final MethodHandles.Lookup lookup = lookup();

    private Reflection() { }

    @SuppressWarnings({"unchecked", "Since15"})
    static <C> Class<? extends C> defineSubclass(final Class<C> clazz, final String name, final byte[] b) {
        try {
            final MethodHandles.Lookup lookup = privateLookupIn(null != clazz.getClassLoader() ? clazz : Proxies.class,
                    Reflection.lookup);
            return (Class<? extends C>) lookup.defineClass(b);
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
            } else if (clazz == Character.TYPE) {
                return Character.class;
            } else if (clazz == Byte.TYPE) {
                return Byte.class;
            } else if (clazz == Double.TYPE) {
                return Double.class;
            } else if (clazz == Float.TYPE) {
                return Float.class;
            } else if (clazz == Long.TYPE) {
                return Long.class;
            } else if (clazz == Integer.TYPE) {
                return Integer.class;
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

    static Collection<Method> overridableMethods(final Class<?> clazz) {
        final Map<String, Method> methods = new LinkedHashMap<>();
        traverse(c -> {
            for (final Method method : c.getDeclaredMethods()) {
                if (0 == (method.getModifiers() & PRIVATE_STATIC_VOLATILE)) {
                    methods.putIfAbsent(signature(method), method);
                }
            }
        }).accept(clazz);
        final Collection<Method> values = methods.values();
        values.removeIf(method -> 0 != (method.getModifiers() & FINAL));
        return values;
    }

    /**
     * Returns a consumer which applies the given consumer to all elements of the type hierarchy represented by its
     * class parameter.
     * The traversal starts with calling the given consumer for the given type, then applies itself recursively to the
     * superclass of the given type (if existing) and finally to all of the interfaces implemented by the given type
     * (if any).
     * Note that due to interfaces, the type hierarchy can be a graph.
     * The returned function will visit any interface at most once, however.
     */
    private static Consumer<Class<?>> traverse(Consumer<Class<?>> consumer) {
        return new Consumer<Class<?>>() {

            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public void accept(final Class<?> visitor) {
                consumer.accept(visitor);
                final Class<?> zuper = visitor.getSuperclass();
                if (null != zuper) {
                    accept(zuper);
                }
                for (final Class<?> iface : visitor.getInterfaces()) {
                    if (!interfaces.contains(iface)) {
                        accept(iface);
                        interfaces.add(iface);
                    }
                }
            }
        };
    }

    private static String signature(Method method) {
        return method.getName() + methodDescriptorWithoutReturnType(method);
    }

    private static String methodDescriptorWithoutReturnType(Method method) {
        return getMethodDescriptor(method).replaceAll("\\).*", ")");
    }
}
