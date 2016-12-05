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
import global.namespace.neuron.di.java.NeuronDIException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

final class NeuronProxyFactory<N> implements Function<NeuronProxyContext<N>, N> {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodType dependencyProviderObjectSignature = MethodType.methodType(DependencyProvider.class, Object.class);
    private static final MethodType voidObjectDependencyProviderSignature = MethodType.methodType(Void.TYPE, Object.class, DependencyProvider.class);
    private static final MethodType objectSignature = MethodType.methodType(Object.class);

    private final Class<? extends N> neuronProxyClass;
    private final MethodHandle constructorHandle;
    private final List<MethodHandler> methodHandlers;

    NeuronProxyFactory(final Class<? extends N> neuronClass, final List<Method> providerMethods) {
        this.neuronProxyClass = ASM.neuronProxyClass(neuronClass, providerMethods);
        try {
            final Constructor<?> c = neuronProxyClass.getDeclaredConstructor();
            c.setAccessible(true);
            this.constructorHandle = lookup.unreflectConstructor(c).asType(objectSignature);
        } catch (ReflectiveOperationException e) {
            throw new NeuronDIException(e);
        }
        this.methodHandlers = providerMethods.stream().map(MethodHandler::new).collect(Collectors.toList());
    }

    public N apply(final NeuronProxyContext<N> ctx) {
        return new Visitor<N>() {

            final N neuronProxy = ctx.cast(neuronProxy());

            BoundMethodHandler boundMethodHandler;

            N apply() {
                for (final MethodHandler handler : methodHandlers) {
                    this.boundMethodHandler = handler.bind(neuronProxy);
                    ctx.element(handler.method()).accept(this);
                }
                return neuronProxy;
            }

            public void visitSynapse(SynapseElement<N> element) {
                boundMethodHandler.setProvider(element.decorate(ctx.provider(element.method())));
            }

            public void visitMethod(MethodElement<N> element) {
                boundMethodHandler.setProvider(element.decorate(boundMethodHandler.getProvider()));
            }
        }.apply();
    }

    private Object neuronProxy() {
        try {
            return constructorHandle.invokeExact();
        } catch (NullPointerException e) {
            throw new NeuronDIException(e.toString() + ": Does the constructor depend on a synapse method?", e);
        } catch (Throwable e) {
            throw new NeuronDIException(e);
        }
    }

    private final class MethodHandler {

        final Method method;
        final MethodHandle getter, setter;

        MethodHandler(final Method method) {
            this.method = method;
            final String dependencyProviderName = method.getName() + NeuronClassVisitor.PROVIDER;
            try {
                final Field field = neuronProxyClass.getDeclaredField(dependencyProviderName);
                field.setAccessible(true);
                this.getter = lookup.unreflectGetter(field).asType(dependencyProviderObjectSignature);
                this.setter = lookup.unreflectSetter(field).asType(voidObjectDependencyProviderSignature);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        Method method() { return method; }

        BoundMethodHandler bind(final N neuronProxy) {
            return new BoundMethodHandler() {

                public DependencyProvider<?> getProvider() {
                    try {
                        return (DependencyProvider<?>) getter.invokeExact(neuronProxy);
                    } catch (Throwable e) {
                        throw new AssertionError(e);
                    }
                }

                public void setProvider(final DependencyProvider<?> provider) {
                    try {
                        setter.invokeExact(neuronProxy, provider);
                    } catch (Throwable e) {
                        throw new AssertionError(e);
                    }
                }
            };
        }
    }

    private interface BoundMethodHandler {

        DependencyProvider<?> getProvider();
        void setProvider(DependencyProvider<?> provider);
    }
}
