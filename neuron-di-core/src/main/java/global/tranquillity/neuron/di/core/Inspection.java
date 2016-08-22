package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.CachingStrategy;
import global.tranquillity.neuron.di.api.Neuron;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Inspection {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    private final Class<?> runtimeClass;

    private Inspection(final Class<?> runtimeClass) {
        this.runtimeClass = runtimeClass;
    }

    public static Inspection of(Class<?> runtimeClass) {
        return new Inspection(runtimeClass);
    }

    public Operator<Method> withSynapses() {
        return new Operator<Method>() {

            final Element element = element();

            @Override
            public void accept(Consumer<Method> consumer) {
                element.accept(new Visitor() {

                    @Override
                    public void visitSynapse(SynapseElement element) {
                        consumer.accept(element.method());
                    }
                });
            }
        };
    }

    void accept(Visitor visitor) { element().accept(visitor); }

    private Element element() {

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
                        .map(RealCachingStrategy::valueOf)
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
