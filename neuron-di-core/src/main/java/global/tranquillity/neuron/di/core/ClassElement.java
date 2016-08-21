package global.tranquillity.neuron.di.core;

public interface ClassElement extends Element {

    Class<?> runtimeClass();

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitClass(value, this);
    }
}
