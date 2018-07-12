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
import global.namespace.neuron.di.java.MethodBinding;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

final class ProxyFactory<C> implements Function<MethodBinding, C>{

    private static final MethodType dependencyProviderObjectMethodType =
            methodType(DependencyProvider.class, Object.class);

    private static final MethodType voidObjectDependencyProviderMethodType =
            methodType(Void.TYPE, Object.class, DependencyProvider.class);

    private static final MethodType objectMethodType =
            methodType(Object.class);

    private final Class<? extends C> neuronProxyClass;
    private final MethodHandle constructorHandle;
    private final List<MethodHandler> methodHandlers;

    ProxyFactory(final Class<? extends C> neuronClass, final List<MethodElement<C>> bindableElements) {
        this.neuronProxyClass = ASM.proxyClass(neuronClass, map(bindableElements, MethodElement<C>::method));
        try {
            final Constructor<?> c = neuronProxyClass.getDeclaredConstructor();
            c.setAccessible(true);
            this.constructorHandle = publicLookup().unreflectConstructor(c).asType(objectMethodType);
        } catch (ReflectiveOperationException e) {
            throw new BreedingException(e);
        }
        this.methodHandlers = map(bindableElements, MethodHandler::new);
    }

    private static <T, U> List<U> map(List<T> list, Function<? super T, ? extends U> fun) {
        return list.stream().map(fun).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public C apply(final MethodBinding binding) {
        return new Visitor<C>() {

            final C neuronProxy;

            BoundMethodHandler boundMethodHandler;

            {
                try {
                    neuronProxy = (C) constructorHandle.invokeExact();
                } catch (Throwable e) {
                    throw new BreedingException(e);
                }
                for (final MethodHandler handler : methodHandlers) {
                    boundMethodHandler = handler.bind(neuronProxy);
                    handler.accept(this);
                }
            }

            @Override
            public void visitSynapse(SynapseElement<C> element) {
                boundMethodHandler.provider(element.decorate(element.apply(binding)
                        .orElseThrow(() -> new BreedingException("No binding defined for synapse method: " + element.method()))));
            }

            @Override
            public void visitMethod(MethodElement<C> element) {
                boundMethodHandler.provider(element.decorate(element.apply(binding)
                        .orElseGet(boundMethodHandler::provider)));
            }

        }.neuronProxy;
    }

    private final class MethodHandler {

        final MethodElement<C> element;
        final MethodHandle getter, setter;

        MethodHandler(final MethodElement<C> element) {
            this.element = element;
            final String dependencyProviderName = element.name() + ProxyClassVisitor.PROVIDER;
            final MethodHandles.Lookup lookup = publicLookup();
            try {
                final Field field = neuronProxyClass.getDeclaredField(dependencyProviderName);
                field.setAccessible(true);
                this.getter = lookup.unreflectGetter(field).asType(dependencyProviderObjectMethodType);
                this.setter = lookup.unreflectSetter(field).asType(voidObjectDependencyProviderMethodType);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        BoundMethodHandler bind(final C neuronProxy) {
            return new BoundMethodHandler() {

                @Override
                public DependencyProvider<?> provider() {
                    try {
                        return (DependencyProvider<?>) getter.invokeExact(neuronProxy);
                    } catch (Throwable e) {
                        throw new AssertionError(e);
                    }
                }

                @Override
                public void provider(final DependencyProvider<?> provider) {
                    try {
                        setter.invokeExact(neuronProxy, provider);
                    } catch (Throwable e) {
                        throw new AssertionError(e);
                    }
                }
            };
        }

        void accept(Visitor<C> visitor) { element.accept(visitor); }
    }

    private interface BoundMethodHandler {

        DependencyProvider<?> provider();

        void provider(DependencyProvider<?> provider);
    }
}
