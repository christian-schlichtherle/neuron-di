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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static global.namespace.neuron.di.internal.NeuronElement.isCachingEligible;

/**
 * Adapts a function which accepts a class object reflecting a super class
 * and an array of class objects reflecting interfaces to a function which
 * accepts a class object reflecting a class or interface.
 * <p>
 * The adapted function is supposed to call some CGLIB method, which typically
 * accept a super class and an array of interfaces whereas this function accepts
 * a class or interface.
 *
 * @param <V> the return type of the adapted function.
 */
final class CGAdapterFunction<V> implements Function<Class<?>, V> {

    private static Class<?>[] NO_CLASSES = new Class<?>[0];

    private final BiFunction<Class<?>, Class<?>[], V> function;

    /**
     * @param function a function which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     *                 Returns an arbitrary value.
     */
    CGAdapterFunction(final BiFunction<Class<?>, Class<?>[], V> function) {
        this.function = function;
    }

    /** Calls the adapted function and returns its value. */
    @Override
    public V apply(Class<?> runtimeClass) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (runtimeClass.isInterface()) {
            if (isTraitWithNonAbstractMembers(runtimeClass)) {
                throw new UnsupportedOperationException("Trait with non-abstract members: " + runtimeClass);
            } else if (hasCachingEligibleDefaultMethods(runtimeClass)) {
                superclass = createClass(runtimeClass);
                interfaces = NO_CLASSES;
            } else {
                superclass = Object.class;
                interfaces = new Class<?>[] { runtimeClass };
            }
        } else {
            superclass = runtimeClass;
            interfaces = NO_CLASSES;
        }
        return function.apply(superclass, interfaces);
    }

    private static boolean isTraitWithNonAbstractMembers(final Class<?> trait) {
        assert trait.isInterface();
        return anyMatch(type -> {
            final ClassLoader loader = type.getClassLoader();
            if (null != loader) {
                try {
                    loader.loadClass(type.getName() + "$class");
                    return true;
                } catch (ClassNotFoundException ignored) {
                }
            }
            return false;
        }).apply(trait);
    }

    private static boolean hasCachingEligibleDefaultMethods(final Class<?> iface) {
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
     * Note that due to interfaces, the type hierarchy can be a graph.
     * The returned function will visit any interface at most once, however.
     */
    private static Function<Class<?>, Boolean> anyMatch(final Predicate<Class<?>> predicate) {
        return new Function<Class<?>, Boolean>() {

            final Set<Class<?>> interfaces = new HashSet<>();

            @Override
            public Boolean apply(final Class<?> clazz) {
                if (predicate.test(clazz)) {
                    return true;
                }
                final Class<?> zuper = clazz.getSuperclass();
                if (null != zuper) {
                    if (apply(zuper)) {
                        return true;
                    }
                }
                for (final Class<?> iface : clazz.getInterfaces()) {
                    if (!interfaces.contains(iface)) {
                        interfaces.add(iface);
                        if (apply(iface)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    private static Class<?> createClass(final Class<?> iface) {
        assert iface.isInterface();
        final Enhancer e = new Enhancer();
        e.setSuperclass(Object.class);
        e.setInterfaces(new Class<?>[] { iface });
        e.setCallbackType(NoOp.class);
        e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
        e.setUseCache(false);
        e.setUseFactory(false);
        return e.createClass();
    }
}
