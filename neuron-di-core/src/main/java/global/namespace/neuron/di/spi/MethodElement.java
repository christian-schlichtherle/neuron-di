package global.namespace.neuron.di.spi;

import java.lang.reflect.Method;

interface MethodElement extends Element, HasCachingStrategy {

    Method method();

    @Override
    default void accept(Visitor visitor) { visitor.visitMethod(this); }
}
