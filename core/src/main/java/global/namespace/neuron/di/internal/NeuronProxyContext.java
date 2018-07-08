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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

final class NeuronProxyContext<N> {

    private final NeuronElement<N> element;

    NeuronProxyContext(final NeuronElement<N> element) { this.element = element; }

    NeuronProxyFactory<N> factory() {
        final Class<? extends N> adaptedClass = adaptedClass();
        return new NeuronProxyFactory<>(adaptedClass, bindableElements(adaptedClass));
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

    private List<MethodElement<N>> bindableElements(Class<? extends N> neuronClass) {
        return new Visitor<N>() {

            final Collection<Method> methods =
                    new OverridableMethodsCollector(neuronClass.getPackage()).collect(neuronClass);

            final ArrayList<MethodElement<N>> bindableElements = new ArrayList<>(methods.size());

            {
                methods.forEach(method -> element(method).accept(this));
                bindableElements.trimToSize();
            }

            @Override
            public void visitSynapse(SynapseElement<N> element) { bindableElements.add(element); }

            @Override
            public void visitMethod(final MethodElement<N> element) {
                if (!element.hasParameters() && !element.isVoid()) {
                    bindableElements.add(element);
                }
            }
        }.bindableElements;
    }

    private MethodElement<N> element(Method method) { return element.element(method); }
}
