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
package global.namespace.neuron.di.spi;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** A real incubator {@linkplain #breed(Class, Function) breeds} neurons. */
public final class RealIncubator {

    private static final Map<Class<?>, CglibHack<?>> hacks =
            Collections.synchronizedMap(new WeakHashMap<>());

    private RealIncubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily.
     *
     * @param bind a function which looks up a binding for a given synapse
     *             method (the injection point) and returns some supplier to
     *             resolve the dependency.
     *             The {@code bind} function is called before the call to
     *             {@code breed} returns in order to look up the binding
     *             eagerly.
     *             The returned supplier is called later when the synapse method
     *             is accessed in order to resolve the dependency lazily.
     *             Depending on the caching strategy for the synapse method, the
     *             supplied dependency may get cached for future use.
     */
    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, Supplier<?>> bind) {

        class ClassVisitor implements Visitor {

            private T instance;

            @SuppressWarnings("unchecked")
            @Override
            public void visitNeuron(final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                final CglibHack<T> hack = (CglibHack<T>) hacks.computeIfAbsent(runtimeClass,
                        key -> new CglibHack<>(runtimeClass, element::element, bind));
                instance = hack.newInstance(element::element, bind);
            }

            @Override
            public void visitClass(final ClassElement element) {
                assert runtimeClass == element.runtimeClass();
                instance = createInstance(runtimeClass);
            }
        }

        final ClassVisitor visitor = new ClassVisitor();
        Inspection.of(runtimeClass).accept(visitor);
        return visitor.instance;
    }

    private static <T> T createInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw (InstantiationError)
                    new InstantiationError(e.getMessage() + ": Did you forget the @Neuron annotation?").initCause(e);
        } catch (IllegalAccessException e) {
            throw (IllegalAccessError)
                    new IllegalAccessError(e.getMessage()).initCause(e);
        }
    }
}
