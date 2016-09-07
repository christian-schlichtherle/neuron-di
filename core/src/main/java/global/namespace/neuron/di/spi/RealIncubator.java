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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/** A real incubator {@linkplain #breed(Class, Function) breeds} neurons. */
public class RealIncubator {

    private RealIncubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily.
     *
     * @param bind a function which looks up a binding for a given synapse
     *             method (the injection point) and returns some supplier or
     *             function to resolve the dependency.
     *             The {@code bind} function is called before the call to
     *             {@code breed} returns in order to look up the binding
     *             eagerly.
     *             The returned supplier or function is called later when the
     *             synapse method is accessed in order to resolve the dependency
     *             lazily.
     *             If a function is provided, its parameter will be the instance
     *             returned by {@code breed}.
     *             Depending on the caching strategy for the synapse method, the
     *             resolved dependency may get cached for future use.
     */
    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, ?> bind) {

        class ClassVisitor implements Visitor {

            private T instance;

            @Override
            public void visitNeuron(final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                instance = new CglibFunction<>((superclass, interfaces) -> {

                    class MethodVisitor
                            extends CallbackHelper
                            implements Visitor {

                        private Callback callback;

                        private MethodVisitor() {
                            super(superclass, interfaces);
                        }

                        @Override
                        protected Callback getCallback(Method method) {
                            element.element(method).accept(this);
                            assert null != callback;
                            return callback;
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void visitSynapse(final SynapseElement element) {
                            final Object resolve = bind.apply(element.method());
                            if (resolve instanceof Supplier) {
                                callback = element.synapseCallback(
                                        ((Supplier<?>) resolve)::get);
                            } else {
                                callback = element.synapseCallback(
                                        (obj, method, args) -> ((Function<? super T, ?>) resolve).apply((T) obj));
                            }
                        }

                        @Override
                        public void visitMethod(MethodElement element) {
                            callback = element.methodCallback();
                        }
                    }

                    return runtimeClass.cast(createProxy(superclass, interfaces, new MethodVisitor()));
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

    private static Object createProxy(final Class<?> superclass,
                                      final Class<?>[] interfaces,
                                      final CallbackHelper helper) {
        final Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(helper);
        e.setCallbacks(helper.getCallbacks());
        e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
        return e.create();
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
