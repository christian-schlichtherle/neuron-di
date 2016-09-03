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

public class IncubatorImpl {

    private IncubatorImpl() { }

    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, Supplier<?>> bind) {

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

                        @Override
                        public void visitSynapse(final SynapseElement element) {
                            final Supplier<?> supplier = bind.apply(element.method());
                            callback = element.synapseCallback(supplier::get);
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
