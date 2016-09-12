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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static global.namespace.neuron.di.internal.NeuronElement.isCachingEligible;

interface Reflection {

    static boolean isTraitWithNonAbstractMembers(final Class<?> trait) {
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

    static boolean hasCachingEligibleDefaultMethods(final Class<?> iface) {
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
    static Function<Class<?>, Boolean> anyMatch(final Predicate<Class<?>> predicate) {
        return hierarchy -> {
            try {
                traverse(type -> {
                    if (predicate.test(type)) {
                        throw new PredicateMatchException();
                    }
                }).accept(hierarchy);
                return false;
            } catch (PredicateMatchException match) {
                return true;
            }
        };
    }

    /**
     * Returns a consumer which applies the given consumer to all elements of
     * the type hierarchy represented by its class parameter.
     * Note that due to interfaces, the type hierarchy can be a graph.
     * The returned function will visit any interface at most once, however.
     */
    static Consumer<Class<?>> traverse(final Consumer<Class<?>> consumer) {
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
}
