package global.tranquillity.neuron.di.core;

import java.lang.reflect.Method;

public interface MethodElement extends Element, HasCachingStrategy {

    Method method();

    default Class<?> methodReturnType() { return method().getReturnType(); }

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitMethod(value, this);
    }
}
