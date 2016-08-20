package global.tranquillity.neuron.di.api;

public interface Visitor<V> {

    default V visitClass(V value, ClassElement element) { return value; }

    default V visitNeuron(V value, NeuronElement element) {
        return element.traverse(value, this);
    }

    default V visitMethod(V value, MethodElement element) { return value; }

    default V visitSynapse(V value, SynapseElement element) { return value; }
}
