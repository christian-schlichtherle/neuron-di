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

import global.namespace.neuron.di.java.DependencyProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class NeuronProxyContext<N> {

    private final NeuronElement<N> element;
    private final Function<Method, DependencyProvider<?>> binding;

    NeuronProxyContext(final NeuronElement<N> element,
                       final Function<Method, DependencyProvider<?>> binding) {
        this.element = element;
        this.binding = binding;
    }

    MethodElement<N> element(Method method) { return element.element(method); }

    DependencyProvider<?> provider(Method method) { return binding.apply(method); }

    NeuronProxyFactory<N> factory() {
        final Class<? extends N> adaptedClass = adaptedClass();
        return new NeuronProxyFactory<>(adaptedClass, providerMethods(adaptedClass));
    }

    private Class<? extends N> adaptedClass() {
        final Class<N> neuronClass = neuronClass();
        return Optional
                .ofNullable(neuronClass.getDeclaredAnnotation(Shim.class))
                .<Class<? extends N>>map(this::shimClass)
                .orElse(neuronClass);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends N> shimClass(final Shim annotation) {
        Class<? extends N> shimClass;
        try {
            shimClass = (Class<? extends N>) neuronClass().getClassLoader().loadClass(annotation.name());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        if (shimClass == Object.class) {
            shimClass = (Class<? extends N>) annotation.value();
        }
        if (shimClass == Object.class) {
            throw new IllegalStateException("The @Shim annotation on " + neuronClass() + " must reference a @Neuron class.");
        }
        return shimClass;
    }

    private Class<N> neuronClass() { return element.runtimeClass(); }

    private List<Method> providerMethods(Class<? extends N> neuronClass) {
        return new Visitor<N>() {

            final Collection<Method> methods = new OverridableMethodsCollector(neuronClass.getPackage()).collect(neuronClass);

            final ArrayList<Method> filtered = new ArrayList<>(methods.size());

            List<Method> apply() {
                methods.forEach(method -> element(method).accept(this));
                filtered.trimToSize();
                return filtered;
            }

            @Override
            public void visitSynapse(SynapseElement<N> element) { filtered.add(element.method()); }

            @Override
            public void visitMethod(final MethodElement<N> element) {
                if (element.isCachingEnabled()) {
                    filtered.add(element.method());
                }
            }
        }.apply();
    }
}
