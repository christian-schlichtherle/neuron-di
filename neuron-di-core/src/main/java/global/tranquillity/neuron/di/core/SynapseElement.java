package global.tranquillity.neuron.di.core;

public interface SynapseElement extends MethodElement {

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitSynapse(value, this);
    }
}
