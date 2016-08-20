package global.tranquillity.neuron.di.api;

import java.lang.reflect.Method;

public interface MethodElement extends Element, HasCachingStrategy {

    Method method();

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitMethod(value, this);
    }
}
