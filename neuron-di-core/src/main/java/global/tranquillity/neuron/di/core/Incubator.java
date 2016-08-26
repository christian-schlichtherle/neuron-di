package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public class Incubator {

    private Incubator() { }

    public static <T> Stubbing<T> stub(final Class<T> runtimeClass) {
        return new Stubbing<T>() {

            final List<Entry<Function<T, ?>, Function<? super T, ?>>> bindings =
                    new LinkedList<>();

            Map<Method, Supplier<Function<? super T, ?>>> replacementSuppliers;

            T neuron;

            Function<? super T, ?> currentReplacement;

            @Override
            public <U> MethodStubbing<T, U> bind(final Function<T, U> methodReference) {
                return replacement -> {
                    bindings.add(new SimpleImmutableEntry<>(methodReference, replacement));
                    return this;
                };
            }

            @Override
            public T breed() {
                synchronized (this) {
                    if (null != neuron) {
                        throw new IllegalStateException("`breed()` has already been called");
                    }
                    replacementSuppliers = new HashMap<>(bindings.size() * 4 / 3 + 1);
                    neuron = Incubator.breed(runtimeClass, this::binder);
                }
                exploreMethodReferences();
                return neuron;
            }

            void exploreMethodReferences() {
                try {
                    for (final Entry<Function<T, ?>, Function<? super T, ?>> binding : bindings) {
                        final Function<T, ?> methodReference = binding.getKey();
                        currentReplacement = binding.getValue();
                        try {
                            methodReference.apply(neuron);
                            assert false;
                        } catch (ControlFlowError ignored) {
                        }
                    }
                } finally {
                    currentReplacement = null;
                }
            }

            Supplier<Object> binder(final Method method) {
                final Supplier<Function<? super T, ?>> replacementSupplier =
                        replacementSuppliers.computeIfAbsent(method, this::replacementSupplier);
                return () -> replacementSupplier.get().apply(neuron);
            }

            Supplier<Function<? super T, ?>> replacementSupplier(final Method method) {
                return new Supplier<Function<? super T, ?>>() {

                    Function<? super T, ?> replacement;

                    @Override
                    public Function<? super T, ?> get() {
                        if (null != currentReplacement) {
                            replacement = currentReplacement;
                            throw new ControlFlowError();
                        } else {
                            if (null != replacement) {
                                return replacement;
                            } else {
                                throw new IllegalStateException(
                                        "Insufficient stubbing: No binding defined for method `" + method + "` in neuron `" + runtimeClass + "`.");
                            }
                        }
                    }
                };
            }
        };
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily by recursively calling this method.
     */
    public static <T> T breed(Class<T> runtimeClass) {
        return breed(runtimeClass, synapse -> {
            final Class<?> returnType = synapse.getReturnType();
            return () -> breed(returnType);
        });
    }

    /**
     * Returns a new instance of the given runtime class which will resolve its
     * dependencies lazily.
     * This method is usually called from plugins for DI frameworks in order to
     * integrate Neuron DI into the framework.
     * The {@code binder} function then typically calls back into the DI
     * framework in order to look up a binding for the synapse method (the
     * injection point) and returns a supplier for the resolved dependency.
     *
     * @param binder a function which looks up a binding for a given synapse
     *               method (the injection point) and returns a supplier for the
     *               resolved dependency.
     *               When evaluating the function or the supplier, the
     *               implementation may recursively call this method again.
     */
    public static <T> T breed(final Class<T> runtimeClass,
                              final Function<Method, Supplier<?>> binder) {

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
                            final Supplier<?> binding = binder.apply(element.method());
                            callback = element.synapseCallback(binding::get);
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

    public interface Stubbing<T> {

        <U> MethodStubbing<T, U> bind(Function<T, U> methodReference);

        T breed();
    }

    public interface MethodStubbing<T, U> {

        default Stubbing<T> to(U value) { return to(neuron -> value); }

        Stubbing<T> to(Function<? super T, ? extends U> replacement);
    }
}
