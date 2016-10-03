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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class NeuronProxyContext<T> {

    private final NeuronElement element;
    private final Function<Method, Supplier<?>> binding;

    NeuronProxyContext(final NeuronElement element,
                       final Function<Method, Supplier<?>> binding) {
        this.element = element;
        this.binding = binding;
    }

    MethodElement element(Method method) { return element.element(method); }

    Supplier<?> supplier(Method method) { return binding.apply(method); }

    <U> U apply(final BiFunction<Class<?>, Class<?>[], U> function) {
        return new ClassAdapter<>(function).apply(neuronClass());
    }

    @SuppressWarnings("unchecked")
    private Class<T> neuronClass() { return (Class<T>) element.runtimeClass(); }
}
