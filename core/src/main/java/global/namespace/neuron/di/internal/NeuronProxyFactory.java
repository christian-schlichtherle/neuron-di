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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class NeuronProxyFactory<T> implements Function<NeuronProxyContext, Object> {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodType objectMethodType = MethodType.methodType(Object.class);

    private Map<Method, Field> methodProxyFields;
    private Class<?> neuronProxyClass;
    private final MethodHandle neuronProxyConstructor;

    NeuronProxyFactory(final NeuronProxyContext<T> ctx) {
        this.methodProxyFields = ctx.apply((superclass, interfaces) -> {
            final List<Method> proxiedMethods = MethodProxyFilter.from(superclass, interfaces).proxiedMethods(ctx);
            this.neuronProxyClass = ASM.neuronProxyClass(superclass, interfaces, proxiedMethods);
            return proxiedMethods;
        }).stream().collect(LinkedHashMap::new, (map, method) -> map.put(method, field(method)), Map::putAll);
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
                    .asType(objectMethodType);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private Field field(final Method method) {
        final String fieldName = method.getName() + "$proxy";
        final Field field;
        try {
            field = neuronProxyClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
        field.setAccessible(true);
        return field;
    }

    public Object apply(final NeuronProxyContext ctx) {
        final Object neuronProxy = neuronProxy();
        new Visitor() {

            void init() {
                for (Method method : methodProxyFields.keySet()) {
                    ctx.element(method).accept(this);
                }
            }

            public void visitSynapse(final SynapseElement element) {
                final Supplier<?> resolve = ctx.supplier(element.method());
                final Field field = methodProxyFields.get(element.method());
                setMethodProxy(field, element.decorate(resolve::get));
            }

            public void visitMethod(final MethodElement element) {
                final Field field = methodProxyFields.get(element.method());
                setMethodProxy(field, element.decorate(getMethodProxy(field)));
            }

            MethodProxy<?, ?> getMethodProxy(final Field field) {
                try {
                    return (MethodProxy<?, ?>) field.get(neuronProxy);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }

            void setMethodProxy(final Field field, final MethodProxy<?, ?> proxy) {
                try {
                    field.set(neuronProxy, proxy);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }.init();
        return neuronProxy;
    }

    @SuppressWarnings("unchecked")
    private T neuronProxy() {
        try {
            return (T) neuronProxyConstructor.invokeExact();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
