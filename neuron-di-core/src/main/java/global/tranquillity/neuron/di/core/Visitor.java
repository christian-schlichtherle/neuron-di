package global.tranquillity.neuron.di.core;

interface Visitor<V> {

    default V visitNeuron(V value, NeuronElement element) {
        return element.traverseMethods(value, this);
    }

    default V visitClass(V value, ClassElement element) { return value; }

    default V visitSynapse(V value, SynapseElement element) { return value; }

    default V visitMethod(V value, MethodElement element) { return value; }
}
