package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.CachingStrategy;
import global.tranquillity.neuron.di.api.Neuron;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Inspection {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    public static Operator<Method> withSynapsesOf(final Class<?> runtimeClass) {
        return new Operator<Method>() {

            final Element element = inspect(runtimeClass);

            @Override
            public <V> V foldLeft(final V initialValue, final BiFunction<V, Method, V> accumulator) {

                class SynapseVisitor implements Visitor {

                    private V value = initialValue;

                    @Override
                    public void visitSynapse(SynapseElement element) {
                        value = accumulator.apply(value, element.method());
                    }
                }

                final SynapseVisitor visitor = new SynapseVisitor();
                element.accept(visitor);
                return visitor.value;
            }
        };
    }

    static Element inspect(final Class<?> runtimeClass) {

        class ClassBase {

            public Class<?> runtimeClass() { return runtimeClass; }
        }

        final Neuron neuron = runtimeClass.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement extends ClassBase implements NeuronElement {

                @Override
                public CachingStrategy cachingStrategy() {
                    return neuron.cachingStrategy();
                }
            }

            return new RealNeuronElement();
        } else {

            class RealClassElement extends ClassBase implements ClassElement { }

            return new RealClassElement();
        }
    }

    /**
     * Adapts a function which accepts a class object reflecting a super class
     * and an array of class objects reflecting interfaces to a function which
     * accepts a class object reflecting a class or interface.
     * <p>
     * The given function is supposed to call some CGLIB method, which typically
     * accept a super class and an array of interfaces whereas the returned
     * function accepts a class or interface.
     *
     * @param function a function which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     *                 Returns an arbitrary value.
     * @param <V> the return type of {@code function}.
     * @return a function which accepts a class object reflecting a class or
     *         interface.
     *         Returns the return value of {@code function}.
     */
    static <V> Function<Class<?>, V> cglibAdapter(
            final BiFunction<Class<?>, Class<?>[], V> function) {
        return runtimeClass -> {
            final Class<?> superclass;
            final Class<?>[] interfaces;
            if (runtimeClass.isInterface()) {
                if (hasDefaultMethodsWhichRequireCaching(runtimeClass)) {
                    superclass = createClass(runtimeClass);
                    interfaces = NO_CLASSES;
                } else {
                    superclass = Object.class;
                    interfaces = new Class<?>[]{runtimeClass};
                }
            } else {
                superclass = runtimeClass;
                interfaces = NO_CLASSES;
            }
            return function.apply(superclass, interfaces);
        };
    }

    private static boolean hasDefaultMethodsWhichRequireCaching(final Class<?> iface) {
        assert iface.isInterface();
        for (final Method method : iface.getDeclaredMethods()) {
            if (method.isDefault()) {
                return cachingStrategyOption(method)
                        .map(Inspection::realCachingStrategy)
                        .filter(RealCachingStrategy::isEnabled)
                        .isPresent();
            }
        }
        for (final Class<?> superInterface : iface.getInterfaces()) {
            if (hasDefaultMethodsWhichRequireCaching(superInterface)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> createClass(final Class<?> iface) {
        assert iface.isInterface();
        final Enhancer e = new Enhancer();
        e.setSuperclass(Object.class);
        e.setInterfaces(new Class<?>[] { iface });
        e.setCallbackType(NoOp.class);
        return e.createClass();
    }

    static Object createProxy(final Class<?> superclass,
                              final Class<?>[] interfaces,
                              final CallbackHelper helper) {
        final Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(helper);
        e.setCallbacks(helper.getCallbacks());
        return e.create();
    }

    static <T> T createInstance(final Class<T> clazz) {
        try {
            final Constructor<T> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
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
            Unsafe.getUnsafe().throwException(e.getTargetException());
            throw new AssertionError("Unreachable statement.", e);
        }
    }

    static RealCachingStrategy realCachingStrategy(CachingStrategy strategy) {
        return RealCachingStrategy.valueOf(strategy.name());
    }

    static boolean isParameterless(Method method) {
        return 0 == method.getParameterCount();
    }

    static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    static Optional<CachingStrategy> cachingStrategyOption(Method method) {
        return Optional
                .ofNullable(method.getAnnotation(Caching.class))
                .map(Caching::value);
    }
}
