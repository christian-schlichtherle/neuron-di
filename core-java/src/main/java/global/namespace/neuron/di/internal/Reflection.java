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
import global.namespace.neuron.di.java.BreedingException;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static java.lang.reflect.Modifier.*;
import static java.util.Optional.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

final class Reflection {

    private static final MethodHandles.Lookup lookup = lookup();

    @SuppressWarnings("unchecked")
    static <C> Class<? extends C> defineSubclass(final Class<C> clazz, final byte[] b) {
        try {
            return (Class<? extends C>) privateLookupIn(null != clazz.getClassLoader() ? clazz : Proxies.class, lookup)
                    .defineClass(b);
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
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
        final Collection<Method> methods = overridableMethodsMap(clazz).values();
        // VOLATILE methods are bridge methods inserted by the compiler, e.g. when inheriting from a generic superclass
        // and specifying its type parameter in the subclass.
        methods.removeIf(method -> 0 != (method.getModifiers() & (FINAL | VOLATILE)));
        return methods;
    }

    private static Map<String, Method> overridableMethodsMap(final Class<?> clazz) {
        final Map<String, Method> methods = new LinkedHashMap<>();
        traverse(c -> {
            for (final Method method : c.getDeclaredMethods()) {
                if (0 == (method.getModifiers() & (PRIVATE | STATIC))) {
                    methods.merge(signature(method), method, Reflection::select);
                }
            }
        }).accept(clazz);
        return methods;
    }

    private static Method select(Method old, Method noo) {
        return old.getDeclaringClass().isAssignableFrom(noo.getDeclaringClass()) &&
                old.getReturnType().isAssignableFrom(noo.getReturnType())
                ? noo
                : old;
    }

    /**
     * Returns a consumer which applies the given consumer to all elements of the type hierarchy represented by its
     * class parameter.
     * The traversal starts with calling the given consumer for the given type, then applies itself recursively to the
     * superclass of the given type (if existing) and finally to all of the interfaces implemented by the given type
     * (if any).
     * Note that due to interfaces, the type hierarchy can be a graph;
     * the returned consumer will visit any interface at most once, however.
     */
    private static Consumer<Class<?>> traverse(Consumer<Class<?>> consumer) {
        return new Consumer<Class<?>>() {

            final Set<Class<?>> visited = new HashSet<>();

            @Override
            public void accept(final Class<?> clazz) {
                if (visited.add(clazz)) {
                    consumer.accept(clazz);
                    ofNullable(clazz.getSuperclass()).ifPresent(this);
                    for (Class<?> iface : clazz.getInterfaces()) {
                        accept(iface);
                    }
                }
            }
        };
    }

    private static String signature(Method method) {
        return method.getName() + getMethodDescriptor(method).replaceAll("\\).*", ")");
    }

    static <T extends Annotation> Function<AnnotatedElement, Optional<T>> findAnnotation(Class<T> what) {
        return new Function<AnnotatedElement, Optional<T>>() {

            final Set<AnnotatedElement> visited = new HashSet<>();

            @SuppressWarnings("unchecked")
            @Override
            public Optional<T> apply(final AnnotatedElement where) {
                if (visited.add(where)) {
                    for (final Annotation a : where.getAnnotations()) {
                        if (what.isInstance(a)) {
                            return of((T) a);
                        }
                        final Optional<T> here = apply(a.annotationType());
                        if (here.isPresent()) {
                            return here;
                        }
                    }
                }
                return empty();
            }
        };
    }

    private Reflection() {
    }
}
