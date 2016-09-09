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

    private static final Map<Class<?>, Hack> filterAndFactories =
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
                final Hack factory = filterAndFactories
                        .computeIfAbsent(runtimeClass,
                                new CglibFunction<>((superclass, interfaces) -> {
                                    final Hack hack = new Hack(superclass, interfaces);
                                    final Enhancer e = new Enhancer();
                                    e.setSuperclass(superclass);
                                    e.setInterfaces(interfaces);
                                    e.setCallbackFilter(hack.filter);
                                    e.setCallbacks(invalidate(hack.callbacks = hack.callbacks(bind, element::element)));
                                    e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
                                    e.setUseCache(false);
                                    hack.factory = (Factory) e.create();
                                    return hack;
                                })
                        );
                instance = runtimeClass.cast(factory.newInstance(bind, element::element));
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

    private static class Hack {

        final Filter filter;
        Factory factory;
        Callback[] callbacks;

        Hack(final Class<?> superclass, final Class<?>[] interfaces) {
            filter = new Filter(superclass, interfaces);
        }

        Object newInstance(Function<Method, Supplier<?>> bind,
                           Function<Method, MethodElement> elements) {
            return factory.newInstance(callbackSupplier(bind, elements).get());
        }

        private synchronized Supplier<Callback[]> callbackSupplier(
                final Function<Method, Supplier<?>> bind,
                final Function<Method, MethodElement> elements) {
            if (null != callbacks) {
                final Callback[] c = callbacks;
                callbacks = null;
                return () -> c;
            } else {
                return () -> callbacks(bind, elements);
            }
        }

        private Callback[] callbacks(Function<Method, Supplier<?>> bind,
                             Function<Method, MethodElement> elements) {
            return filter.callbacks(bind, elements);
        }
    }

    private static class Filter implements CallbackFilter {

        private final ArrayList<Method> methods;

        Filter(final Class<?> superclass, final Class<?> interfaces[]) {
            methods = new ArrayList<>();
            Enhancer.getMethods(superclass, interfaces, methods);
            methods.trimToSize();
        }

        Callback[] callbacks(final Function<Method, Supplier<?>> bind,
                             final Function<Method, MethodElement> elements) {
            final Callback[] callbacks = new Callback[methods.size()];
            int i = 0;
            for (final Method method : methods) {
                final int index = i++;
                elements.apply(method).accept(new Visitor() {

                    @Override
                    public void visitSynapse(SynapseElement element) {
                        final Supplier<?> resolve = bind.apply(element.method());
                        callbacks[index] = element.synapseCallback(resolve::get);
                    }

                    @Override
                    public void visitMethod(MethodElement element) {
                        callbacks[index] = element.methodCallback();
                    }
                });
            }
            return callbacks;
        }

        @Override
        public int accept(Method method) {
            // This method is called at most once per runtime class and method,
            // so we can easily afford the overall runtime complexity of O(n*n),
            // where n is the number of methods, for the benefit of reduced
            // overhead when iterating through the methods.
            return methods.indexOf(method);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Filter)) return false;
            final Filter that = (Filter) obj;
            return this.methods.equals(that.methods);
        }

        @Override
        public int hashCode() { return methods.hashCode(); }
    }
}
