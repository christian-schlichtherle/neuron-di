package global.tranquillity.neuron.di.core;

interface Visitor {

    default void visitNeuron(NeuronElement element) {
        element.traverseMethods(this);
    }

    default void visitClass(ClassElement element) { }

    default void visitSynapse(SynapseElement element) { }

    default void visitMethod(MethodElement element) { }
}
