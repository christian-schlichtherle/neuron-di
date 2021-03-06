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

import static global.namespace.neuron.di.internal.Reflection.overridableMethods;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;

final class ProxyContext<C> {

    private final ClassElement<C> element;

    ProxyContext(final ClassElement<C> element) {
        element.assertCanBeProxied();
        this.element = element;
    }

    ProxyFactory<C> factory() {
        final Class<? extends C> adaptedClass = adaptedClass();
        return new ProxyFactory<>(adaptedClass, bindableElements(adaptedClass));
    }

    private Class<? extends C> adaptedClass() {
        final Class<C> clazz = clazz();
        return Optional
                .ofNullable(clazz.getDeclaredAnnotation(Shim.class))
                .<Class<? extends C>>map(this::shimClass)
                .orElse(clazz);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends C> shimClass(final Shim annotation) {
        Class<? extends C> shimClass;
        try {
            shimClass = (Class<? extends C>) clazz().getClassLoader().loadClass(annotation.name());
        } catch (ClassNotFoundException e) {
            throw new BreedingException(e);
        }
        if (shimClass == Object.class) {
            shimClass = (Class<? extends C>) annotation.value();
        }
        if (shimClass == Object.class) {
            throw new BreedingException("The @Shim annotation must reference a @Neuron class: " + clazz());
        }
        return shimClass;
    }

    private Class<C> clazz() {
        return element.clazz();
    }

    private List<MethodElement<C>> bindableElements(Class<? extends C> clazz) {
        return new Visitor<C>() {

            final ArrayList<MethodElement<C>> bindableElements;

            {
                // Package-private methods of bootstrap classes cannot be bound because their packages are sealed, so
                // you cannot define a subclass in the same package:
                final Package pkg = null != clazz.getClassLoader() ? clazz.getPackage() : null;
                final Collection<Method> methods = overridableMethods(clazz);
                bindableElements = new ArrayList<>(methods.size());
                methods.forEach(method -> {
                    if (0 != (method.getModifiers() & (PROTECTED | PUBLIC))
                            || method.getDeclaringClass().getPackage() == pkg) {
                        element.element(method).accept(this);
                    }
                });
                bindableElements.trimToSize();
            }

            @Override
            public void visitSynapse(SynapseElement<C> element) {
                bindableElements.add(element);
            }

            @Override
            public void visitMethod(final MethodElement<C> element) {
                if (!element.hasParameters() && !element.isVoid()) {
                    bindableElements.add(element);
                }
            }
        }.bindableElements;
    }
}
