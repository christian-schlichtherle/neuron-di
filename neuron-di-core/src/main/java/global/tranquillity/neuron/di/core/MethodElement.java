package global.tranquillity.neuron.di.core;

import java.lang.reflect.Method;

interface MethodElement extends Element, HasCachingStrategy {

    Method method();

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitMethod(value, this);
    }
}
