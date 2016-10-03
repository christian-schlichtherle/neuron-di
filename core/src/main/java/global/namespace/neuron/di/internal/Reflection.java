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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

class Reflection {

    private static final Method getClassLoadingLock, defineClass;

    static {
        final Class<ClassLoader> classLoaderClass = ClassLoader.class;
        try {
            getClassLoadingLock = classLoaderClass
                    .getDeclaredMethod("getClassLoadingLock", String.class);
            getClassLoadingLock.setAccessible(true);
            defineClass = classLoaderClass
                    .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

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
        return new Consumer<Class<?>>() {

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
        };
    }

    @SuppressWarnings("unchecked")
    static <T> Class<? extends T> defineSubclass(final Class<T> clazz,
                                                 final String name,
                                                 final byte[] b) {
        final ClassLoader cl = associatedClassLoader(clazz);
        try {
            synchronized (getClassLoadingLock.invoke(cl, name)) {
                return (Class<? extends T>) defineClass.invoke(cl, name, b, 0, b.length);
            }
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    static ClassLoader associatedClassLoader(Class<?> clazz) {
        return Optional
                .ofNullable(clazz.getClassLoader())
                .orElse(Optional
                        .ofNullable(Thread.currentThread().getContextClassLoader())
                        .orElse(Optional
                                .ofNullable(ClassLoader.getSystemClassLoader())
                                .orElseThrow(() -> new IllegalArgumentException("No class loader associated with " + clazz))));
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
