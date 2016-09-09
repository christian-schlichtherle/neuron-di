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

import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** A real incubator {@linkplain #breed(Class, Function) breeds} neurons. */
public class RealIncubator {

    private static final Map<Class<?>, Filter> filters =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<Class<?>, Factory> factories =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final FixedValue INVALID_FIXED_VALUE =
            () -> { throw new AssertionError(); };

    private static final MethodInterceptor INVALID_METHOD_INTERCEPTOR =
            (obj, method, args, proxy) -> { throw new AssertionError(); };

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

            @Override
            public void visitNeuron(final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                instance = new CglibFunction<>((superclass, interfaces) -> {

                    final Filter filter = filters.computeIfAbsent(runtimeClass, ignored -> {
                        final ArrayList<Method> methods = new ArrayList<>();
                        Enhancer.getMethods(superclass, interfaces, methods);
                        return new Filter(methods);
                    });

                    final Callback[] callbacks = new Callback[filter.size()];
                    for (Method method : filter.methods()) {
                        element.element(method).accept(new Visitor() {

                            final int index = filter.accept(method);

                            @Override
                            public void visitSynapse(SynapseElement element) {
                                final Supplier<?> resolve = bind.apply(method);
                                callbacks[index] = element.synapseCallback(resolve::get);
                            }

                            @Override
                            public void visitMethod(MethodElement element) {
                                callbacks[index] = element.methodCallback();
                            }
                        });
                    }

                    final Factory factory = factories.computeIfAbsent(runtimeClass, ignored -> {
                        final Enhancer e = new Enhancer();
                        e.setSuperclass(superclass);
                        e.setInterfaces(interfaces);
                        e.setCallbackFilter(filter);
                        e.setCallbacks(invalidate(callbacks));
                        e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
                        e.setUseCache(false);
                        return (Factory) e.create();
                    });

                    return runtimeClass.cast(factory.newInstance(callbacks));
                }).apply(runtimeClass);
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

    private static Callback[] invalidate(final Callback[] callbacks) {
        final Callback[] results = callbacks.clone();
        for (int i = results.length; --i >= 0; ) {
            final Callback result = results[i];
            if (result instanceof FixedValue) {
                results[i] = INVALID_FIXED_VALUE;
            } else if (result instanceof MethodInterceptor) {
                results[i] = INVALID_METHOD_INTERCEPTOR;
            }
        }
        return results;
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

    private static class Filter implements CallbackFilter {

        private final Map<Method, Integer> indexes;

        Filter(final List<Method> methods) {
            this.indexes = new LinkedHashMap<>(methods.size() / 3 * 4 + 1);
            int i = 0;
            for (Method method : methods) {
                indexes.put(method, i++);
            }
        }

        int size() { return indexes.size(); }

        Iterable<Method> methods() { return indexes.keySet(); }

        @Override
        public int accept(Method method) { return indexes.get(method); }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Filter)) return false;
            final Filter that = (Filter) obj;
            return this.indexes.equals(that.indexes);
        }

        @Override
        public int hashCode() { return indexes.hashCode(); }
    }
}
