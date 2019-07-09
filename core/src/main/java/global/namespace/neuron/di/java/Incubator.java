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

import global.namespace.neuron.di.internal.MethodBinding;
import global.namespace.neuron.di.internal.MethodInfo;
import global.namespace.neuron.di.internal.RealIncubator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static global.namespace.neuron.di.java.Reflection.methodHandle;
import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;

/**
 * An incubator breeds neuron types.
 * A neuron type is a class or interface with the {@link Neuron} annotation.
 */
public final class Incubator {

    private static final Lookup publicLookup = publicLookup();

    private Incubator() {
    }

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
     *                resolve its return value lazily.
     *                Depending on the caching strategy for the synapse method, the resolved dependency may get cached
     *                for subsequent calls to the synapse method.
     */
    public static <T> T breed(Class<T> clazz, Function<Method, DependencyProvider<?>> binding) {
        return RealIncubator.breed(clazz, info -> info.isAbstract() ? of(binding.apply(info.method())) : empty());
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

            final Map<DependencyResolver<T, ?>, Object> bindings = new LinkedHashMap<>();

            boolean partial;

            @Override
            public Wire<T> partial(final boolean value) {
                this.partial = value;
                return this;
            }

            @Override
            public <U> Bind<T, U> bind(final DependencyResolver<T, U> methodReference) {
                return new Bind<T, U>() {

                    @Override
                    public Wire<T> to(U value) {
                        return to(() -> value);
                    }

                    @Override
                    public Wire<T> to(DependencyProvider<? extends U> provider) {
                        bindings.put(methodReference, provider);
                        return wire();
                    }

                    @Override
                    public Wire<T> to(DependencyResolver<? super T, ? extends U> resolver) {
                        bindings.put(methodReference, resolver);
                        return wire();
                    }
                };
            }

            Wire<T> wire() {
                return this;
            }

            @Override
            public T using(Object delegate, Function<Method, String> namer) {
                return using(requireNonNull(delegate), requireNonNull(namer), publicLookup);
            }

            @SuppressWarnings("SameParameterValue")
            private T using(final Object delegate, final Function<Method, String> namer, final Lookup lookup) {
                partial = true;
                return breed(delegate, namer, lookup);
            }

            @Override
            public T breed() {
                return breed(null, null, null);
            }

            @SuppressWarnings("unchecked")
            private T breed(final Object delegate, final Function<Method, String> namer, final Lookup lookup) {
                return new Object() {

                    final T neuron = RealIncubator.breed(clazz, new MethodBinding() {

                        final Map<MethodInfo, Object> resolvedBindings = new Resolver<>(clazz).resolve(bindings);

                        @Override
                        public Optional<DependencyProvider<?>> apply(final MethodInfo info) {
                            final Optional<DependencyProvider<?>> odp = ofNullable(resolvedBindings.get(info))
                                    .map(o -> (o instanceof DependencyProvider)
                                            ? (DependencyProvider<?>) o
                                            : () -> ((DependencyResolver<? super T, ?>) o).apply(neuron));
                            if (odp.isPresent() || !info.isAbstract()) {
                                return odp;
                            } else if (!partial) {
                                throw new BreedingException(
                                        "Partial binding is disabled and no binding is defined for synapse method: " + info.method());
                            } else if (null != delegate) {
                                assert null != namer;
                                assert null != lookup;
                                final MethodHandle handle = methodHandle(namer.apply(info.method()), delegate, lookup);
                                return of(handle::invokeExact);
                            } else {
                                return of(() -> Incubator.breed(info.returnType()));
                            }
                        }
                    });
                }.neuron;
            }
        };
    }

    /**
     * A wire statement.
     * It's an error to implement this interface outside of the {@code Incubator} class!
     *
     * @param <T> the type of the neuron to wire.
     */
    public interface Wire<T> {

        /**
         * Enables or disables partial binding.
         * By default, partial binding is disabled, resulting in a {@link BreedingException} when breeding a neuron and
         * there is no binding defined for some synapse methods.
         */
        Wire<T> partial(boolean value);

        /**
         * Binds the synapse method identified by the given method reference.
         */
        <U> Bind<T, U> bind(DependencyResolver<T, U> methodReference);

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         * Otherwise, the neuron will forward any calls to unbound synapse methods to the given {@code delegate} object.
         * The delegate object may provide the dependencies as a method or field, even if this member is private or
         * static.
         * Members with the same names will be recursively searched for starting with the runtime class of the
         * given delegate object.
         * The delegate method or field will be {@linkplain Method#setAccessible(boolean) made accessible} and
         * transformed into a method handle using a {@linkplain MethodHandles#publicLookup() public lookup} object.
         * Some effort is spent to avoid illegal reflective access.
         */
        default T using(Object delegate) {
            return using(delegate, Method::getName);
        }

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         * Otherwise, the neuron will forward any calls to unbound synapse methods to the given {@code delegate} object.
         * The delegate object may provide the dependencies as a method or field, even if this member is private or
         * static.
         * The name of the member will be determined by calling the given {@code namer} function.
         * Members with the resulting names will be recursively searched for starting with the runtime class of the
         * given delegate object.
         * The delegate method or field will be {@linkplain Method#setAccessible(boolean) made accessible} and
         * transformed into a method handle using a {@linkplain MethodHandles#publicLookup() public lookup} object.
         * Some effort is spent to avoid illegal reflective access.
         */
        T using(Object delegate, Function<Method, String> namer);

        /**
         * Breeds the wired neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         */
        T breed();
    }

    /**
     * A bind statement.
     * It's an error to implement this interface outside of the {@code Incubator} class!
     *
     * @param <T> the type of the neuron to wire.
     * @param <U> the type of the synapse method to bind.
     */
    public interface Bind<T, U> {

        /**
         * Binds the synapse method to the given value.
         */
        Wire<T> to(U value);

        /**
         * Binds the synapse method to the given provider.
         */
        Wire<T> to(DependencyProvider<? extends U> provider);

        /**
         * Binds the synapse method to the given function.
         */
        Wire<T> to(DependencyResolver<? super T, ? extends U> resolver);
    }
}
