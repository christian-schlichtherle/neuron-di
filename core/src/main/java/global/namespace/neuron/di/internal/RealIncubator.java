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

import global.namespace.neuron.di.java.DependencySupplier;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class RealIncubator {

    private static final Map<Class<?>, NeuronProxyFactory<?>> factories =
            Collections.synchronizedMap(new WeakHashMap<>());

    private RealIncubator() { }

    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, DependencySupplier<?>> binding) {

        class ClassVisitor implements Visitor<T> {

            private T instance;

            @SuppressWarnings("unchecked")
            @Override
            public void visitNeuron(final NeuronElement<T> element) {
                assert runtimeClass == element.runtimeClass();
                final NeuronProxyContext<T> ctx = new NeuronProxyContext<>(element, binding);
                instance = ((NeuronProxyFactory<T>) factories
                        .computeIfAbsent(runtimeClass, key -> new NeuronProxyFactory<>(ctx)))
                        .apply(ctx);
            }

            @Override
            public void visitClass(final ClassElement<T> element) {
                assert runtimeClass == element.runtimeClass();
                instance = createInstance(runtimeClass);
            }
        }

        final ClassVisitor visitor = new ClassVisitor();
        Inspection.of(runtimeClass).accept(visitor);
        return visitor.instance;
    }

    private static <T> T createInstance(final Class<T> runtimeClass) {
        try {
            return runtimeClass.newInstance();
        } catch (InstantiationException e) {
            throw (InstantiationError)
                    new InstantiationError(e.getMessage() + ": Did you forget the @Neuron annotation?").initCause(e);
        } catch (IllegalAccessException e) {
            throw (IllegalAccessError)
                    new IllegalAccessError(e.getMessage()).initCause(e);
        }
    }
}
