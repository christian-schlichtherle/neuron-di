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

import global.namespace.neuron.di.java.DependencySupplier;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class NeuronProxyContext<T> {

    private final NeuronElement<T> element;
    private final Function<Method, DependencySupplier<?>> binding;

    NeuronProxyContext(final NeuronElement<T> element,
                       final Function<Method, DependencySupplier<?>> binding) {
        this.element = element;
        this.binding = binding;
    }

    MethodElement<T> element(Method method) { return element.element(method); }

    DependencySupplier<?> supplier(Method method) { return binding.apply(method); }

    <U> U apply(Function<Class<? extends T>, U> function) {
        return new ClassAdapter<>(function).apply(neuronType());
    }

    T cast(Object obj) { return neuronType().cast(obj); }

    private Class<T> neuronType() { return element.runtimeClass(); }

    List<Method> proxiedMethods(final Class<? extends T> neuronType) {
        final OverridableMethodsCollector collector = new OverridableMethodsCollector(neuronType.getPackage())
                .add(neuronType);
        return new Visitor<T>() {

            final List<Method> methods = collector.result();

            final ArrayList<Method> filtered = new ArrayList<>(methods.size());

            public void visitSynapse(final SynapseElement<T> element) { filtered.add(element.method()); }

            public void visitMethod(final MethodElement<T> element) {
                if (element.isCachingEnabled()) {
                    filtered.add(element.method());
                }
            }

            List<Method> apply() {
                for (Method method : methods) {
                    element(method).accept(this);
                }
                filtered.trimToSize();
                return filtered;
            }
        }.apply();
    }
}
