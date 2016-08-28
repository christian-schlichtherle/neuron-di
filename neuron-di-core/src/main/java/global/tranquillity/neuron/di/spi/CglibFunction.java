package global.tranquillity.neuron.di.spi;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import static global.tranquillity.neuron.di.spi.NeuronElement.isCachingEligible;

/**
 * Adapts a function which accepts a class object reflecting a super class
 * and an array of class objects reflecting interfaces to a function which
 * accepts a class object reflecting a class or interface.
 * <p>
 * The adapted function is supposed to call some CGLIB method, which typically
 * accept a super class and an array of interfaces whereas this function accepts
 * a class or interface.
 *
 * @param <V> the return type of the adapted function.
 */
class CglibFunction<V> implements Function<Class<?>, V> {

    private static Class<?>[] NO_CLASSES = new Class<?>[0];

    private final BiFunction<Class<?>, Class<?>[], V> function;

    /**
     * @param function a function which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     *                 Returns an arbitrary value.
     */
    CglibFunction(final BiFunction<Class<?>, Class<?>[], V> function) {
        this.function = function;
    }

    /** Calls the adapted function and returns its value. */
    @Override
    public V apply(Class<?> runtimeClass) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (runtimeClass.isInterface()) {
            if (hasCachingEligibleDefaultMethods(runtimeClass)) {
                superclass = createClass(runtimeClass);
                interfaces = NO_CLASSES;
            } else {
                superclass = Object.class;
                interfaces = new Class<?>[] { runtimeClass };
            }
        } else {
            superclass = runtimeClass;
            interfaces = NO_CLASSES;
        }
        return function.apply(superclass, interfaces);
    }

    private static boolean hasCachingEligibleDefaultMethods(final Class<?> iface) {
        assert iface.isInterface();
        for (final Method method : iface.getDeclaredMethods()) {
            if (method.isDefault() && isCachingEligible(method)) {
                return true;
            }
        }
        for (final Class<?> superInterface : iface.getInterfaces()) {
            if (hasCachingEligibleDefaultMethods(superInterface)) {
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
}
