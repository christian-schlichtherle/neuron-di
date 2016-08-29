package global.namespace.neuron.di.spi;

interface Visitor {

    default void visitNeuron(NeuronElement element) {
        element.traverseMethods(this);
    }

    default void visitClass(ClassElement element) { }

    default void visitSynapse(SynapseElement element) { }

    default void visitMethod(MethodElement element) { }
}
