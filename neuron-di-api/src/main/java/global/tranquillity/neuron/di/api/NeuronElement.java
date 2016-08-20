package global.tranquillity.neuron.di.api;

public interface NeuronElement extends ClassElement, HasCachingStrategy {

    <V> V traverse(V value, Visitor<V> visitor);

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitNeuron(value, this);
    }
}
