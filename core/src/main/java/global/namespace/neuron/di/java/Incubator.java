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
     * This method is usually called from plugins for DI frameworks in order to integrate Neuron DI into the DI
     * framework.
     *
     * @param binding a function which looks up a binding for a given synapse method (the injection point) and returns
     *                some supplier to resolve the dependency.
     *                The {@code binding} function is called before the call to {@code breed} returns in order to look
     *                up the binding eagerly.
     *                The returned supplier is called later when the synapse method is accessed in order to resolve the
     *                dependency lazily.
     *                Depending on the caching strategy for the synapse method, the supplied dependency may get cached
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
     * neuron, the binding definitions will be examined eagerly in order to create suppliers for resolving the
     * dependencies lazily.
     * The suppliers will use the bound {@linkplain Bind#to(Object) values},
     * {@linkplain Bind#to(DependencyProvider) suppliers} or {@linkplain Bind#to(DependencyFunction) functions}.
     * <p>
     * If the given runtime class is not a neuron class or interface, then adding bindings will have no effect and when
     * breeding, the incubator will just create a new instance of the given class using the public constructor without
     * parameters.
     */
    public static <T> Stub<T> stub(final Class<T> runtimeClass) {
        return new Stub<T>() {

            boolean partial;
            List<Entry<Function<T, ?>, DependencyFunction<? super T, ?>>> bindings =
                    new LinkedList<>();

            List<Method> synapses = new LinkedList<>();
            T neuron;

            DependencyFunction<? super T, ?> currentReplacement;
            int currentPosition;

            @Override
            public Stub<T> partial(final boolean value) {
                this.partial = value;
                return this;
            }

            @Override
            public <U> Bind<T, U> bind(final Function<T, U> methodReference) {
                return replacement -> {
                    bindings.add(new SimpleImmutableEntry<>(methodReference, replacement));
                    return this;
                };
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
                if (!partial && !synapses.isEmpty()) {
                    throw new IllegalStateException(
                            "Partial stubbing is disabled and no binding is defined for some synapse methods: " + synapses);
                }
                // Support GC:
                assert null == currentReplacement;
                bindings = null;
                synapses = null;
                return neuron;
            }

            DependencyProvider<?> binding(final Method method) {
                synapses.add(method);
                return new DependencyProvider<Object>() {

                    DependencyFunction<? super T, ?> replacement;

                    @Override
                    public Object get() throws Throwable {
                        if (null == replacement) {
                            if (null != currentReplacement) {
                                replacement = currentReplacement;
                                final boolean removed = synapses.remove(method);
                                assert removed;
                                throw new BindingSuccessException();
                            } else {
                                replacement = neuron -> Incubator.breed(method.getReturnType());
                            }
                        }
                        return replacement.apply(neuron);
                    }
                };
            }

            void initReplacementProxies() {
                try {
                    for (final Entry<Function<T, ?>, DependencyFunction<? super T, ?>> binding : bindings) {
                        final Function<T, ?> methodReference = binding.getKey();
                        currentReplacement = binding.getValue();
                        currentPosition++;
                        try {
                            methodReference.apply(neuron);
                            throw new IllegalStateException("Illegal stubbing: The function parameter of the `bind` call at position " + currentPosition + " does not call a synapse method.");
                        } catch (BindingSuccessException ignored) {
                        }
                    }
                } finally {
                    currentReplacement = null;
                }
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
        <U> Bind<T, U> bind(Function<T, U> methodReference);

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

        /** Binds the synapse method to the given supplier. */
        default Stub<T> to(DependencyProvider<? extends U> supplier) { return to(neuron -> supplier.get()); }

        /** Binds the synapse method to the given function. */
        Stub<T> to(DependencyFunction<? super T, ? extends U> function);
    }
}
