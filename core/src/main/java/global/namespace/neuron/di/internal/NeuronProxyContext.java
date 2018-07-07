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

import global.namespace.neuron.di.java.BreedingException;
import global.namespace.neuron.di.java.DependencyProvider;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class NeuronProxyContext<N> {

    private final NeuronElement<N> element;
    private final Function<Method, Optional<DependencyProvider<?>>> binding;

    NeuronProxyContext(final NeuronElement<N> element,
                       final Function<Method, Optional<DependencyProvider<?>>> binding) {
        this.element = element;
        this.binding = binding;
    }

    MethodElement<N> element(Method method) { return element.element(method); }

    Optional<DependencyProvider<?>> provider(MethodElement<N> element) { return binding.apply(element.method()); }

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
            throw new BreedingException(e);
        }
        if (shimClass == Object.class) {
            shimClass = (Class<? extends N>) annotation.value();
        }
        if (shimClass == Object.class) {
            throw new BreedingException("The @Shim annotation must reference a @Neuron class: " + neuronClass());
        }
        return shimClass;
    }

    private Class<N> neuronClass() { return element.runtimeClass(); }

    private Set<Method> providerMethods(Class<? extends N> neuronClass) {
        return new Visitor<N>() {

            final Collection<Method> methods =
                    new OverridableMethodsCollector(neuronClass.getPackage()).collect(neuronClass);

            final Set<Method> filtered = new LinkedHashSet<>((methods.size() + 2) * 4 / 3);

            Set<Method> apply() {
                methods.forEach(method -> element(method).accept(this));
                return filtered;
            }

            @Override
            public void visitSynapse(SynapseElement<N> element) { filtered.add(element.method()); }

            @Override
            public void visitMethod(final MethodElement<N> element) {
                if (!element.hasParameters() && !element.isVoid()) {
                    if (element.isCachingEnabled() || provider(element).isPresent()) {
                        filtered.add(element.method());
                    }
                }
            }
        }.apply();
    }
}
