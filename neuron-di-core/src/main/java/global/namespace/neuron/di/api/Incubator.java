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
package global.namespace.neuron.di.api;

import global.namespace.neuron.di.spi.IncubatorImpl;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An incubator {@linkplain #breed(Class) breeds} neurons.
 * For testing purposes, it can also {@linkplain #stub(Class) stub} neurons.
 */
public class Incubator {

    private Incubator() { }

    public static <T> Stub<T> stub(final Class<T> runtimeClass) {
        return new Stub<T>() {

            final List<Map.Entry<Function<T, ?>, Function<? super T, ?>>> bindings =
                    new LinkedList<>();

            T neuron;

            Function<? super T, ?> currentReplacement;
            int currentPosition;

            @Override
            public <U> Bind<T, U> bind(final Function<T, U> methodReference) {
                return replacement -> {
                    bindings.add(new AbstractMap.SimpleImmutableEntry<>(methodReference, replacement));
                    return this;
                };
            }

            @Override
            public T breed() {
                synchronized (this) {
                    if (null != neuron) {
                        throw new IllegalStateException("`breed()` has already been called");
                    }
                    neuron = Incubator.breed(runtimeClass, this::resolve);
                }
                initReplacementProxies();
                return neuron;
            }

            Supplier<Object> resolve(final Method method) {
                final Supplier<Function<? super T, ?>> replacementProxy =
                        replacementProxy(method);
                return () -> replacementProxy.get().apply(neuron);
            }

            Supplier<Function<? super T, ?>> replacementProxy(final Method method) {
                return new Supplier<Function<? super T, ?>>() {

                    Function<? super T, ?> replacement;

                    @Override
                    public Function<? super T, ?> get() {
                        if (null != replacement) {
                            return replacement;
                        } else {
                            replacement = currentReplacement;
                            if (null != replacement) {
                                throw new ControlFlowError();
                            } else {
                                throw new IllegalStateException(
                                        "Insufficient stubbing: No binding defined for method `" + method + "` in neuron `" + runtimeClass + "`.");
                            }
                        }
                    }
                };
            }

            void initReplacementProxies() {
                try {
                    for (final Map.Entry<Function<T, ?>, Function<? super T, ?>> binding : bindings) {
                        final Function<T, ?> methodReference = binding.getKey();
                        currentReplacement = binding.getValue();
                        currentPosition++;
                        try {
                            methodReference.apply(neuron);
                            throw new IllegalStateException("Illegal stubbing at position " + currentPosition + ": The function parameter of the `bind` call does not call a synapse method.");
                        } catch (ControlFlowError ignored) {
                        }
                    }
                } finally {
                    currentReplacement = null;
                }
            }
        };
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily by recursively calling this method.
     */
    public static <T> T breed(Class<T> runtimeClass) {
        return breed(runtimeClass, synapse -> {
            final Class<?> returnType = synapse.getReturnType();
            return () -> breed(returnType);
        });
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily.
     * This method is usually called from plugins for DI frameworks in order to
     * integrate Neuron DI into the framework.
     * The {@code resolve} function then typically calls back into the DI
     * framework in order to look up a binding for the synapse method (the
     * injection point) and return a supplier for the resolved dependency.
     *
     * @param resolve a function which looks up a binding for a given synapse
     *                method (the injection point) and returns a supplier for
     *                the resolved dependency.
     *                When evaluating the function or the supplier, the
     *                implementation may recursively call this method again.
     *                If necessary, this should be done in the function in order
     *                to provide best performance.
     */
    public static <T> T breed(Class<T> runtimeClass,
                              Function<Method, Supplier<?>> resolve) {
        return IncubatorImpl.breed(runtimeClass, resolve);
    }

    public interface Stub<T> {

        /** Binds the synapse method identified by the given method reference. */
        <U> Bind<T, U> bind(Function<T, U> methodReference);

        /**
         * Breeds the stubbed neuron.
         * If the runtime class is not a neuron class or interface, this method
         * tries to call the default constructor.
         */
        T breed();
    }

    public interface Bind<T, U> {

        /** Binds the synapse method to the given replacement value. */
        default Stub<T> to(U replacement) { return to(neuron -> replacement); }

        /** Binds the synapse method to the given replacement supplier. */
        default Stub<T> to(Supplier<? extends U> replacement) {
            return to(neuron -> replacement.get());
        }

        /** Binds the synapse method to the given replacement function. */
        Stub<T> to(Function<? super T, ? extends U> replacement);
    }
}
