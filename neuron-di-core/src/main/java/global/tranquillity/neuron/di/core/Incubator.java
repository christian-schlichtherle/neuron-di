package global.tranquillity.neuron.di.core;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Incubator {

    private Incubator() { }

    public static <T> Stubbing<T> stub(final Class<T> runtimeClass) {
        return new Stubbing<T>() {

            final List<Entry<Function<T, ?>, Function<T, ?>>> stubbings = new LinkedList<>();

            Map<Method, Supplier<Function<T, ?>>> dependencies;

            T neuron;

            @Override
            public <U> Stubbing<T> set(Function<T, U> methodReference, U value) {
                requireNonNull(value);
                return put(methodReference, neuron -> value);
            }

            @Override
            public <U> Stubbing<T> put(final Function<T, U> methodReference, final Function<T, ? extends U> replacement) {
                stubbings.add(new SimpleImmutableEntry<>(methodReference, replacement));
                return this;
            }

            @Override
            public synchronized T breed() {
                if (null != neuron) {
                    throw new IllegalStateException("breed() has already been called");
                }
                dependencies = new HashMap<>(stubbings.size() * 4 / 3 + 1);
                return neuron = Incubator.breed(runtimeClass, this::dependency);
            }

            Object dependency(Method method) {
                return dependencies
                        .computeIfAbsent(method, this::dependencySupplier)
                        .get()
                        .apply(neuron);
            }

            Supplier<Function<T, ?>> dependencySupplier(final Method method) {
                return new Supplier<Function<T, ?>>() {

                    Function<T, ?> function = null;

                    @Override
                    public Function<T, ?> get() {
                        if (null != function) {
                            return function;
                        }
                        function = this::nothing;
                        for (final Entry<Function<T, ?>, Function<T, ?>> stubbing : stubbings) {
                            if (null == stubbing.getKey().apply(neuron)) {
                                return function = stubbing.getValue();
                            }
                        }
                        throw new IllegalStateException("Insufficient stubbing: No stubbing defined for method `" + method + "` in neuron `" + runtimeClass + "`.");
                    }

                    Object nothing(T ignored) { return null; }
                };
            }
        };
    }

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
     * This method is usually called from plugins for DI frameworks in order to
     * integrate Neuron DI into the framework.
     * The {@code dependency} function then calls back into the DI framework in
     * order to look up a binding for the method injection point and eventually
     * recursively call this method again.
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
                            final Method method = element.method();
                            callback = element.synapseCallback(() -> dependency.apply(method));
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

        <U> Stubbing<T> set(Function<T, U> methodReference, U value);

        <U> Stubbing<T> put(Function<T, U> methodReference, Function<T, ? extends U> replacement);

        T breed();
    }
}
