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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class NeuronProxyFactory<T> implements Function<NeuronProxyContext<T>, T> {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodType methodProxyObjectSignature = MethodType.methodType(MethodProxy.class, Object.class);
    private static final MethodType voidObjectMethodProxySignature = MethodType.methodType(Void.TYPE, Object.class, MethodProxy.class);
    private static final MethodType objectSignature = MethodType.methodType(Object.class);

    private List<MethodHandler> methodHandlers;
    private Class<?> neuronProxyClass;
    private final MethodHandle neuronProxyConstructor;

    NeuronProxyFactory(final NeuronProxyContext<T> ctx) {
        this.methodHandlers = ctx.apply((superclass, interfaces) -> {
            final List<Method> proxiedMethods = MethodProxyFilter.from(superclass, interfaces).proxiedMethods(ctx);
            this.neuronProxyClass = ASM.neuronProxyClass(superclass, interfaces, proxiedMethods);
            return proxiedMethods;
        }).stream().map(MethodHandler::new).collect(Collectors.toList());
        final Constructor<?> neuronProxyConstructor;
        try {
            neuronProxyConstructor = neuronProxyClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        neuronProxyConstructor.setAccessible(true);
        try {
            this.neuronProxyConstructor = lookup
                    .unreflectConstructor(neuronProxyConstructor)
                    .asType(objectSignature);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public T apply(final NeuronProxyContext<T> ctx) {
        final T neuronProxy = ctx.cast(neuronProxy());
        new Visitor<T>() {

            MethodHandler.BoundMethodHandler boundMethodHandler;

            void init() {
                for (final MethodHandler handler : methodHandlers) {
                    this.boundMethodHandler = handler.bind(neuronProxy);
                    ctx.element(handler.method()).accept(this);
                }
            }

            public void visitSynapse(final SynapseElement element) {
                final Supplier<?> resolve = ctx.supplier(element.method());
                boundMethodHandler.setMethodProxy(element.decorate(resolve::get));
            }

            public void visitMethod(final MethodElement element) {
                boundMethodHandler.setMethodProxy(element.decorate(boundMethodHandler.getMethodProxy()));
            }
        }.init();
        return neuronProxy;
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
            final String fieldName = method.getName() + "$proxy";
            try {
                final Field field = neuronProxyClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                getter = lookup.unreflectGetter(field).asType(methodProxyObjectSignature);
                setter = lookup.unreflectSetter(field).asType(voidObjectMethodProxySignature);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        Method method() { return method; }

        BoundMethodHandler bind(T neuronProxy) { return new BoundMethodHandler(neuronProxy); }

        final class BoundMethodHandler {

            private final T neuronProxy;

            private BoundMethodHandler(final T neuronProxy) { this.neuronProxy = neuronProxy; }

            MethodProxy<?, ?> getMethodProxy() {
                try {
                    return (MethodProxy<?, ?>) getter.invokeExact(neuronProxy);
                } catch (Throwable e) {
                    throw new AssertionError(e);
                }
            }

            void setMethodProxy(final MethodProxy<?, ?> methodProxy) {
                try {
                    setter.invokeExact(neuronProxy, methodProxy);
                } catch (Throwable e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
}
