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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

final class NeuronProxyFactory<T> implements Function<NeuronProxyContext<T>, T> {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodType dependencyProviderObjectSignature = MethodType.methodType(DependencyProvider.class, Object.class);
    private static final MethodType voidObjectDependencyProviderSignature = MethodType.methodType(Void.TYPE, Object.class, DependencyProvider.class);
    private static final MethodType objectSignature = MethodType.methodType(Object.class);

    private List<MethodHandler> methodHandlers;
    private Class<? extends T> neuronProxyClass;
    private final MethodHandle neuronProxyConstructor;

    NeuronProxyFactory(final NeuronProxyContext<T> ctx) {
        this.methodHandlers = ctx.apply(neuronType -> {
            final List<Method> providerMethods = ctx.providerMethods(neuronType);
            this.neuronProxyClass = ASM.neuronProxyClass(neuronType, providerMethods);
            return providerMethods;
        }).stream().map(MethodHandler::new).collect(Collectors.toList());
        try {
            final Constructor<?> neuronProxyConstructor = neuronProxyClass.getDeclaredConstructor();
            neuronProxyConstructor.setAccessible(true);
            this.neuronProxyConstructor = lookup.unreflectConstructor(neuronProxyConstructor).asType(objectSignature);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public T apply(final NeuronProxyContext<T> ctx) {
        return new Visitor<T>() {

            final T neuronProxy = ctx.cast(neuronProxy());

            MethodHandler.BoundMethodHandler boundMethodHandler;

            T apply() {
                for (final MethodHandler handler : methodHandlers) {
                    this.boundMethodHandler = handler.bind(neuronProxy);
                    ctx.element(handler.method()).accept(this);
                }
                return neuronProxy;
            }

            public void visitSynapse(SynapseElement<T> element) {
                boundMethodHandler.setProvider(element.decorate(ctx.provider(element.method())));
            }

            public void visitMethod(MethodElement<T> element) {
                boundMethodHandler.setProvider(element.decorate(boundMethodHandler.getProvider()));
            }
        }.apply();
    }

    private Object neuronProxy() {
        try {
            return neuronProxyConstructor.invokeExact();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    private final class MethodHandler {

        final Method method;
        final MethodHandle getter, setter;

        MethodHandler(final Method method) {
            this.method = method;
            final String fieldName = method.getName() + "$provider";
            try {
                final Field field = neuronProxyClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                this.getter = lookup.unreflectGetter(field).asType(dependencyProviderObjectSignature);
                this.setter = lookup.unreflectSetter(field).asType(voidObjectDependencyProviderSignature);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        Method method() { return method; }

        BoundMethodHandler bind(T neuronProxy) { return new BoundMethodHandler(neuronProxy); }

        final class BoundMethodHandler {

            private final T neuronProxy;

            private BoundMethodHandler(final T neuronProxy) { this.neuronProxy = neuronProxy; }

            DependencyProvider<?> getProvider() {
                try {
                    return (DependencyProvider<?>) getter.invokeExact(neuronProxy);
                } catch (Throwable e) {
                    throw new AssertionError(e);
                }
            }

            void setProvider(final DependencyProvider<?> provider) {
                try {
                    setter.invokeExact(neuronProxy, provider);
                } catch (Throwable e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
}
