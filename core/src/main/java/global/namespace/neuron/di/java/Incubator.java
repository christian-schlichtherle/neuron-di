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

import global.namespace.neuron.di.internal.RealIncubator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static global.namespace.neuron.di.java.Reflection.find;
import static java.util.Optional.*;

/**
 * An incubator {@linkplain #wire(Class) wires} and {@linkplain #breed(Class) breeds} neuron classes and interfaces.
 *
 * @author Christian Schlichtherle
 */
public final class Incubator {

    private Incubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily by recursively
     * calling this method.
     */
    public static <T> T breed(Class<T> runtimeClass) {
        return make(runtimeClass, synapse -> {
            final Class<?> returnType = synapse.getReturnType();
            return of(() -> breed(returnType));
        });
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily.
     * This method is usually called from plugins or bridges for other DI frameworks in order to integrate Neuron DI
     * into the other DI framework.
     *
     * @param binding a function which looks up a binding for a given synapse method (the injection point) and returns
     *                some provider to resolve the dependency.
     *                The {@code binding} function is called before the call to {@code make} returns in order to look
     *                up the binding eagerly.
     *                The returned provider is called later when the synapse method is accessed in order to resolve the
     *                dependency lazily.
     *                Depending on the caching strategy for the synapse method, the provided dependency may get cached
     *                for future use.
     */
    public static <T> T make(Class<T> runtimeClass, Binding binding) {
        return RealIncubator.breed(runtimeClass, method -> isAbstract(method) ? binding.apply(method) : empty());
    }

    /**
     * Starts constructing an instance of the given runtime class.
     * This is a generic substitute for the {@code new} statement for use with neuron classes and interfaces.
     * Note that the {@code new} statement can neither be used with neuron classes nor interfaces because they are
     * abstract.
     * <p>
     * If the given runtime class is a neuron class or interface, then when {@linkplain Wire#breed() breeding} the
     * neuron, the binding definitions will be examined eagerly in order to create providers for resolving the
     * dependencies lazily.
     * The dependencies will be resolved using {@linkplain Bind#to(Object) values},
     * {@linkplain Bind#to(DependencyProvider) providers} or {@linkplain Bind#to(DependencyResolver) resolvers}.
     * <p>
     * If the given runtime class is not a neuron class or interface, then adding bindings will have no effect and when
     * breeding, the incubator will just create a new instance of the given class using the public constructor without
     * parameters.
     *
     * @since Neuron DI 5.0 (renamed from {@code stub}, which was introduced in Neuron DI 1.0)
     */
    public static <T> Wire<T> wire(final Class<T> runtimeClass) {
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

                    final T neuron = RealIncubator.breed(runtimeClass, new Binding() {

                        final Map<Method, DependencyResolver<? super T, ?>> resolvedBindings =
                                Resolver.resolve(runtimeClass, bindings);

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
                                final String member = method.getName();
                                final MethodHandle handle = find(member).in(delegate).orElseThrow(() ->
                                        new BreedingException("Illegal binding: A member named `" + member + "` neither exists in `" + delegate.getClass() + "` nor in any of its interfaces and superclasses."));
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

    private static boolean isAbstract(Method method ) { return Modifier.isAbstract(method.getModifiers()); }

    @SuppressWarnings("WeakerAccess")
    public interface Wire<T> {

        /**
         * Enables or disables partial binding.
         * By default, partial binding is disabled, resulting in a {@link BreedingException} when breeding a neuron and
         * there is no binding defined for some synapse methods.
         *
         * @since Neuron DI 1.3
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
         *
         * @since Neuron DI 4.5
         */
        T using(Object delegate);

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         */
        T breed();
    }

    @SuppressWarnings("WeakerAccess")
    public interface Bind<T, U> {

        /** Binds the synapse method to the given value. */
        default Wire<T> to(U value) { return to(neuron -> value); }

        /** Binds the synapse method to the given provider. */
        default Wire<T> to(DependencyProvider<? extends U> provider) { return to(neuron -> provider.get()); }

        /** Binds the synapse method to the given function. */
        Wire<T> to(DependencyResolver<? super T, ? extends U> resolver);
    }
}
