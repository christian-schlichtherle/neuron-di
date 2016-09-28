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
import java.util.function.Function;
import java.util.function.Predicate;

import static global.namespace.neuron.di.internal.NeuronElement.isCachingEligible;

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

    static boolean isInterfaceWithCachingDefaultMethods(final Class<?> iface) {
        assert iface.isInterface();
        return anyMatch(type -> {
            for (final Method method : type.getDeclaredMethods()) {
                if (method.isDefault() && isCachingEligible(method)) {
                    return true;
                }
            }
            return false;
        }).apply(iface);
    }

    /**
     * Returns a function which tries to match the given predicate against any
     * element of the type hierarchy represented by its class parameter.
     * The search starts with testing the given predicate for the given type,
     * then applies itself recursively to all of the interfaces implemented by
     * the given type in reverse order (if any) and finally to the superclass of
     * the given type (if existing).
     * Note that due to interfaces, the type hierarchy can be a graph.
     * The returned function will visit any interface at most once, however.
     */
    private static Function<Class<?>, Boolean> anyMatch(final Predicate<Class<?>> predicate) {
        return new Function<Class<?>, Boolean>() {

            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Boolean apply(final Class<?> visitor) {
                if (predicate.test(visitor)) {
                    return true;
                }
                final Class<?>[] ifaces = visitor.getInterfaces();
                for (int i = ifaces.length; 0 <= --i; ) {
                    final Class<?> iface = ifaces[i];
                    if (!interfaces.contains(iface)) {
                        if (apply(iface)) {
                            return true;
                        }
                        interfaces.add(iface);
                    }
                }
                final Class<?> zuper = visitor.getSuperclass();
                if (null != zuper) {
                    if (apply(zuper)) {
                        return true;
                    }
                }
                return false;
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

    static <T> ClassLoader associatedClassLoader(Class<T> clazz) {
        return Optional
                .ofNullable(clazz.getClassLoader())
                .orElse(Optional
                        .ofNullable(Thread.currentThread().getContextClassLoader())
                        .orElse(Optional
                                .ofNullable(ClassLoader.getSystemClassLoader())
                                .orElseThrow(() -> new IllegalArgumentException("No class loader associated with " + clazz))));
    }
}
