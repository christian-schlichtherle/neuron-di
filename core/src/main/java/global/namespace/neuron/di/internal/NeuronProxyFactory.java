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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

final class NeuronProxyFactory<N> implements Function<NeuronProxyContext<N>, N> {

    private static final MethodType dependencyProviderObjectMethodType =
            methodType(DependencyProvider.class, Object.class);

    private static final MethodType voidObjectDependencyProviderMethodType =
            methodType(Void.TYPE, Object.class, DependencyProvider.class);

    private static final MethodType objectMethodType =
            methodType(Object.class);

    private final Class<? extends N> neuronProxyClass;
    private final MethodHandle constructorHandle;
    private final List<MethodHandler> methodHandlers;

    NeuronProxyFactory(final Class<? extends N> neuronClass, final List<Method> providerMethods) {
        this.neuronProxyClass = ASM.neuronProxyClass(neuronClass, providerMethods);
        try {
            final Constructor<?> c = neuronProxyClass.getDeclaredConstructor();
            c.setAccessible(true);
            this.constructorHandle = publicLookup().unreflectConstructor(c).asType(objectMethodType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        this.methodHandlers = providerMethods.stream().map(MethodHandler::new).collect(Collectors.toList());
    }

    public N apply(final NeuronProxyContext<N> ctx) {
        return new Visitor<N>() {

            final Object neuronProxy = neuronProxy();

            BoundMethodHandler boundMethodHandler;

            @SuppressWarnings("unchecked")
            N apply() {
                for (final MethodHandler handler : methodHandlers) {
                    this.boundMethodHandler = handler.bind(neuronProxy);
                    ctx.element(handler.method()).accept(this);
                }
                return (N) neuronProxy;
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
            throw new IllegalStateException(e.toString().concat(": Make sure the (synthetic) constructor does not depend on a synapse method."), e);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
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
                this.getter = publicLookup().unreflectGetter(field).asType(dependencyProviderObjectMethodType);
                this.setter = publicLookup().unreflectSetter(field).asType(voidObjectDependencyProviderMethodType);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        Method method() { return method; }

        BoundMethodHandler bind(final Object neuronProxy) {
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
