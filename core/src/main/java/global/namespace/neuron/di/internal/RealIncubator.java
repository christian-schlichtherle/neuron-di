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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class RealIncubator {

    private static final Map<Class<?>, NeuronProxyFactory<?>> factories =
            Collections.synchronizedMap(new WeakHashMap<>());

    private RealIncubator() { }

    public static <C> C breed(final Class<C> runtimeClass,
                              final Function<Method, DependencyProvider<?>> binding) {

        class ClassVisitor implements Visitor<C> {

            private C instance;

            @SuppressWarnings("unchecked")
            @Override
            public void visitNeuron(final NeuronElement<C> element) {
                assert runtimeClass == element.runtimeClass();
                final NeuronProxyContext<C> ctx = new NeuronProxyContext<>(element, binding);
                final NeuronProxyFactory<C> factory =
                        (NeuronProxyFactory<C>) factories.computeIfAbsent(runtimeClass, key -> ctx.factory());
                instance = factory.apply(ctx);
            }

            @Override
            public void visitClass(final ClassElement<C> element) {
                assert runtimeClass == element.runtimeClass();
                instance = newInstance(runtimeClass);
            }
        }

        final ClassVisitor visitor = new ClassVisitor();
        Inspection.of(runtimeClass).accept(visitor);
        return visitor.instance;
    }

    private static <C> C newInstance(final Class<C> runtimeClass) {
        try {
            return runtimeClass.newInstance();
        } catch (InstantiationException e) {
            throw new NeuronDIException(e.toString() + ": Did you forget the @Neuron annotation?", e);
        } catch (IllegalAccessException e) {
            throw new NeuronDIException(e);
        }
    }
}
