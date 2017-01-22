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
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

/** An incubator {@linkplain #breed(Class) breeds} or {@linkplain #stub(Class) stubs} neurons. */
public final class Incubator {

    private Incubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily by recursively
     * calling this method.
     */
    public static <T> T breed(Class<T> runtimeClass) {
        return breed(runtimeClass, synapse -> {
            final Class<?> returnType = synapse.getReturnType();
            return () -> breed(returnType);
        });
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its dependencies lazily.
     * This method is usually called from plugins or bridges for other DI frameworks in order to integrate Neuron DI
     * into the other DI framework.
     *
     * @param binding a function which looks up a binding for a given synapse method (the injection point) and returns
     *                some provider to resolve the dependency.
     *                The {@code binding} function is called before the call to {@code breed} returns in order to look
     *                up the binding eagerly.
     *                The returned provider is called later when the synapse method is accessed in order to resolve the
     *                dependency lazily.
     *                Depending on the caching strategy for the synapse method, the provided dependency may get cached
     *                for future use.
     */
    public static <T> T breed(Class<T> runtimeClass,
                              Function<Method, DependencyProvider<?>> binding) {
        return RealIncubator.breed(runtimeClass, binding);
    }

    /**
     * Starts constructing an instance of the given runtime class.
     * This is a generic substitute for the {@code new} statement for use with neuron classes and interfaces.
     * Note that the {@code new} statement can neither be used with neuron classes nor interfaces because they are
     * abstract.
     * <p>
     * If the given runtime class is a neuron class or interface, then when {@linkplain Stub#breed() breeding} the
     * neuron, the binding definitions will be examined eagerly in order to create providers for resolving the
     * dependencies lazily.
     * The dependencies will be resolved using {@linkplain Bind#to(Object) values},
     * {@linkplain Bind#to(DependencyProvider) providers} or {@linkplain Bind#to(DependencyResolver) resolvers}.
     * <p>
     * If the given runtime class is not a neuron class or interface, then adding bindings will have no effect and when
     * breeding, the incubator will just create a new instance of the given class using the public constructor without
     * parameters.
     */
    public static <T> Stub<T> stub(final Class<T> runtimeClass) {
        return new Stub<T>() {

            boolean partial;
            List<Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>>> bindings =
                    new LinkedList<>();

            List<Method> synapses = new LinkedList<>();
            T neuron;

            DependencyResolver<? super T, ?> currentResolver;
            int currentPosition;

            Object delegate;

            @Override
            public Stub<T> partial(final boolean value) {
                this.partial = value;
                return this;
            }

            @Override
            public <U> Bind<T, U> bind(final DependencyResolver<T, U> methodReference) {
                return replacement -> {
                    bindings.add(new SimpleImmutableEntry<>(methodReference, replacement));
                    return this;
                };
            }

            @Override
            public T using(final Object delegate) {
                this.delegate = delegate;
                partial = true;
                return breed();
            }

            @Override
            public T breed() {
                synchronized (this) {
                    if (null != neuron) {
                        throw new IllegalStateException("`breed()` has already been called");
                    }
                    neuron = Incubator.breed(runtimeClass, this::binding);
                }

                initReplacementProxies();
                assert null == currentResolver;

                if (!partial && !synapses.isEmpty()) {
                    throw new IllegalStateException(
                            "Partial stubbing is disabled and no binding is defined for some synapse methods: " + synapses);
                }

                // Support garbage collection:
                bindings = null;
                synapses = null;

                return neuron;
            }

            DependencyProvider<?> binding(final Method method) {
                synapses.add(method);
                return new DependencyProvider<Object>() {

                    DependencyResolver<? super T, ?> resolver;

                    @Override
                    public Object get() throws Throwable {
                        if (null == resolver) {
                            if (null != currentResolver) {
                                resolver = currentResolver;
                                final boolean removed = synapses.remove(method);
                                assert removed;
                                throw new BindingSuccessException();
                            } else if (null != delegate) {
                                final MethodHandle handle = dependencyMethodHandle();
                                //noinspection Convert2MethodRef
                                resolver = neuron -> handle.invokeExact();
                            } else {
                                resolver = neuron -> Incubator.breed(method.getReturnType());
                            }
                        }
                        return resolver.apply(neuron);
                    }

                    MethodHandle dependencyMethodHandle() throws NoSuchMethodException {
                        return new Object() {

                            final String name = method.getName();

                            MethodHandle apply() throws NoSuchMethodException {
                                final Method substitute = dependencyMethodIn(delegate.getClass());
                                substitute.setAccessible(true);
                                try {
                                    return publicLookup()
                                            .unreflect(substitute)
                                            .bindTo(delegate)
                                            .asType(methodType(Object.class));
                                } catch (IllegalAccessException e) {
                                    throw new AssertionError(e);
                                }
                            }

                            Method dependencyMethodIn(final Class<?> clazz) throws NoSuchMethodException {
                                try {
                                    return clazz.getDeclaredMethod(name);
                                } catch (final NoSuchMethodException e) {
                                    for (final Class<?> iface : clazz.getInterfaces()) {
                                        try {
                                            return dependencyMethodIn(iface);
                                        } catch (NoSuchMethodException ignored) {
                                        }
                                    }
                                    final Class<?> zuper = clazz.getSuperclass();
                                    if (null == zuper) {
                                        throw e;
                                    }
                                    try {
                                        return dependencyMethodIn(zuper);
                                    } catch (NoSuchMethodException ignored) {
                                        throw e;
                                    }
                                }
                            }
                        }.apply();
                    }
                };
            }

            void initReplacementProxies() {
                try {
                    for (final Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>> binding : bindings) {
                        final DependencyResolver<T, ?> methodReference = binding.getKey();
                        currentResolver = binding.getValue();
                        currentPosition++;
                        try {
                            methodReference.apply(neuron);
                            throw illegalStateException(null);
                        } catch (BindingSuccessException ignored) {
                        } catch (Throwable e) {
                            throw illegalStateException(e);
                        }
                    }
                } finally {
                    currentResolver = null;
                }
            }

            IllegalStateException illegalStateException(Throwable cause) {
                return new IllegalStateException("Illegal stubbing: The function parameter of the `bind` call at position " + currentPosition + " does not call a synapse method.", cause);
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    public interface Stub<T> {

        /**
         * Enables or disables partial stubbing.
         * By default, partial stubbing is disabled, resulting in an {@link IllegalStateException} when breeding a
         * neuron and there is no binding defined for some synapse methods.
         *
         * @since Neuron DI 1.3
         */
        Stub<T> partial(boolean value);

        /** Binds the synapse method identified by the given method reference. */
        <U> Bind<T, U> bind(DependencyResolver<T, U> methodReference);

        /**
         * Breeds the stubbed neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         * Otherwise, the neuron will forward any calls to unbound synapse methods to the given delegate.
         * Note that this feature depends on reflective access to the methods in the delegate.
         * The methods will be set accessible.
         *
         * @since Neuron DI 4.5
         */
        T using(Object delegate);

        /**
         * Breeds the stubbed neuron.
         * If the runtime class is not a neuron class or interface, this method simply calls the default constructor.
         */
        T breed();
    }

    @SuppressWarnings("WeakerAccess")
    public interface Bind<T, U> {

        /** Binds the synapse method to the given value. */
        default Stub<T> to(U value) { return to(neuron -> value); }

        /** Binds the synapse method to the given provider. */
        default Stub<T> to(DependencyProvider<? extends U> provider) { return to(neuron -> provider.get()); }

        /** Binds the synapse method to the given function. */
        Stub<T> to(DependencyResolver<? super T, ? extends U> resolver);
    }
}
