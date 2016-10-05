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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class NeuronProxyContext<N> {

    private final NeuronElement<N> element;
    private final Function<Method, Supplier<?>> binding;

    NeuronProxyContext(final NeuronElement<N> element,
                       final Function<Method, Supplier<?>> binding) {
        this.element = element;
        this.binding = binding;
    }

    MethodElement<N> element(Method method) { return element.element(method); }

    DependencyProvider<?> provider(final Method method) {
        final Supplier<?> supplier = binding.apply(method);
        return supplier::get;
    }

    N cast(Object obj) { return neuronType().cast(obj); }

    <V> V map(BiFunction<Class<? extends N>, List<Method>, V> function) {
        return new ClassAdapter<N, V>(neuronType -> function.apply(neuronType, providerMethods(neuronType)))
                .apply(neuronType());
    }

    private Class<N> neuronType() { return element.runtimeClass(); }

    private List<Method> providerMethods(final Class<? extends N> neuronType) {
        final OverridableMethodsCollector collector = new OverridableMethodsCollector(neuronType.getPackage())
                .add(neuronType);
        return new Visitor<N>() {

            final List<Method> methods = collector.result();

            final ArrayList<Method> filtered = new ArrayList<>(methods.size());

            public void visitSynapse(final SynapseElement<N> element) { filtered.add(element.method()); }

            public void visitMethod(final MethodElement<N> element) {
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
