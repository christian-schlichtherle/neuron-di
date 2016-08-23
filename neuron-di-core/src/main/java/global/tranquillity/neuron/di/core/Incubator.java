package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import static global.tranquillity.neuron.di.core.Inspection.cglibAdapter;

public class Incubator {

    private Incubator() { }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily by recursively calling this method.
     */
    public static <T> T breed(Class<T> runtimeClass) {
        return breed(runtimeClass, synapse -> breed(synapse.getReturnType()));
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily by calling the given function.
     *
     * @param dependency a function which maps a synapse method to its resolved
     *                   dependency.
     */
    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, ?> dependency) {

        class ClassVisitor implements Visitor {

            private T instance;

            @Override
            public void visitNeuron(final NeuronElement element) {
                assert runtimeClass == element.runtimeClass();
                final BiFunction<Class<?>, Class<?>[], T> createProxy = (superclass, interfaces) -> {

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
                            final Method method = element.method();
                            callback = element.synapseCallback(() -> dependency.apply(method));
                        }

                        @Override
                        public void visitMethod(MethodElement element) {
                            callback = element.methodCallback();
                        }
                    }

                    return runtimeClass.cast(createProxy(superclass, interfaces, new MethodVisitor()));
                };
                instance = cglibAdapter(createProxy).apply(runtimeClass);
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
            final Constructor<T> c = clazz.getDeclaredConstructor();
            c.setAccessible(true); // TODO: Why?
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw (InstantiationError)
                    new InstantiationError(clazz.getName()).initCause(e);
        } catch (InstantiationException e) {
            throw (InstantiationError)
                    new InstantiationError(e.getMessage()).initCause(e);
        } catch (IllegalAccessException e) {
            throw (IllegalAccessError)
                    new IllegalAccessError(e.getMessage()).initCause(e);
        } catch (InvocationTargetException e) {
            sun.misc.Unsafe.getUnsafe().throwException(e.getTargetException());
            throw new AssertionError("Unreachable statement.", e);
        }
    }
}
