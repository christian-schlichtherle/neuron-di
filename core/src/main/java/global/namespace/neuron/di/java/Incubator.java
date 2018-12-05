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
package global.namespace.neuron.di.java;

import global.namespace.neuron.di.internal.RealBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static global.namespace.neuron.di.java.Reflection.find;
import static global.namespace.neuron.di.java.Reflection.isAbstract;
import static java.util.Optional.*;

/**
 * An incubator breeds neuron types.
 * A neuron type is a class or interface with the {@link Neuron} annotation.
 *
 * @author Christian Schlichtherle
 */
public final class Incubator {

    private Incubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily by recursively
     * calling this method.
     */
    public static <T> T breed(Class<T> clazz) {
        return breed(clazz, synapse -> {
            final Class<?> returnType = synapse.getReturnType();
            return () -> breed(returnType);
        });
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily.
     * This method is usually called from plugins or bridges for other DI frameworks in order to integrate Neuron DI
     * into the other DI framework.
     *
     * @param binding a function which looks up a binding for a given synapse method (the injection point) to some
     *                dependency provider which resolves the synapse method's return value.
     *                The {@code binding} function is called before the call to {@code breed} returns in order to look
     *                up the dependency provider eagerly.
     *                The returned dependency provider is called later when the synapse method is called in order to
     *                resolve its return value.
     *                Depending on the caching strategy for the synapse method, the resolved dependency may get cached
     *                for subsequent calls to the synapse method.
     */
    public static <T> T breed(Class<T> clazz, SynapseBinding binding) {
        return RealBuilder.breed(clazz, method -> isAbstract(method) ? of(binding.apply(method)) : empty());
    }

    /**
     * Starts breeding an instance of the given runtime class.
     * This is a generic substitute for the {@code new} statement for use with neuron types.
     * Note that the {@code new} statement cannot be used with neuron types because they are abstract.
     * <p>
     * If the given runtime class is a neuron class or interface, then when {@linkplain Wire#breed() breeding} the
     * neuron, the binding definitions will be examined eagerly in order to lookup the dependency providers for
     * lazily resolving the return value of the synapse methods.
     * The dependencies will be resolved using {@linkplain Bind#to(Object) values},
     * {@linkplain Bind#to(DependencyProvider) providers} or {@linkplain Bind#to(DependencyResolver) resolvers}.
     * <p>
     * If the given runtime class is not a neuron class or interface, then adding bindings will have no effect and when
     * breeding, the incubator will just create a new instance of the given class using the public constructor without
     * parameters.
     */
    public static <T> Wire<T> wire(Class<T> clazz) {
        return new Wire<T>() {

            List<Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>>> bindings = new LinkedList<>();

            boolean partial;

            Object delegate;

            @Override
            public Wire<T> partial(final boolean value) {
                this.partial = value;
                return this;
            }

            @Override
            public <U> Bind<T, U> bind(final DependencyResolver<T, U> methodReference) {
                return resolver -> {
                    bindings.add(new SimpleImmutableEntry<>(methodReference, resolver));
                    return this;
                };
            }

            @Override
            public T using(final Object delegate) {
                partial = true;
                this.delegate = delegate;
                return breed();
            }

            @Override
            public T breed() {
                return new Object() {

                    final T neuron = RealBuilder.breed(clazz, new MethodBinding() {

                        final Map<Method, DependencyResolver<? super T, ?>> resolvedBindings =
                                Resolver.resolve(clazz, bindings);

                        @Override
                        public Optional<DependencyProvider<?>> apply(final Method method) {
                            final Optional<DependencyProvider<?>> optionalDependencyProvider =
                                    ofNullable(resolvedBindings.get(method)).map(resolver -> () -> resolver.apply(neuron));
                            if (optionalDependencyProvider.isPresent()) {
                                return optionalDependencyProvider;
                            } else if (!isAbstract(method)) {
                                return empty();
                            } else if (!partial) {
                                throw new BreedingException(
                                        "Partial binding is disabled and no binding is defined for synapse method: " + method);
                            } else if (null != delegate) {
                                final String name = method.getName();
                                final MethodHandle handle = find(name).in(delegate).orElseThrow(() ->
                                        new BreedingException("Illegal binding: A method named `" + name + "` neither exists in `" + delegate.getClass() + "` nor in any of its interfaces and superclasses."));
                                return of(handle::invokeExact);
                            } else {
                                return of(() -> Incubator.breed(method.getReturnType()));
                            }
                        }
                    });
                }.neuron;
            }
        };
    }

    public interface Wire<T> {

        /**
         * Enables or disables partial binding.
         * By default, partial binding is disabled, resulting in a {@link BreedingException} when breeding a neuron and
         * there is no binding defined for some synapse methods.
         */
        Wire<T> partial(boolean value);

        /** Binds the synapse method identified by the given method reference. */
        <U> Bind<T, U> bind(DependencyResolver<T, U> methodReference);

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         * Otherwise, the neuron will forward any calls to unbound synapse methods to the given delegate object.
         * Note that this feature depends on reflective access to the methods in the delegate object.
         * The methods will be set accessible.
         */
        T using(Object delegate);

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         */
        T breed();
    }

    public interface Bind<T, D> {

        /** Binds the synapse method to the given value. */
        default Wire<T> to(D value) { return to(neuron -> value); }

        /** Binds the synapse method to the given provider. */
        default Wire<T> to(DependencyProvider<? extends D> provider) { return to(neuron -> provider.get()); }

        /** Binds the synapse method to the given function. */
        Wire<T> to(DependencyResolver<? super T, ? extends D> resolver);
    }
}
