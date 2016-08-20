package global.tranquillity.neuron.di.api;

public interface ClassElement extends Element {

    Class<?> clazz();

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitClass(value, this);
    }
}
