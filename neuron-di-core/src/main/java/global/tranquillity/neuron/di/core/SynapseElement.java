package global.tranquillity.neuron.di.core;

interface SynapseElement extends MethodElement {

    @Override
    default void accept(Visitor visitor) { visitor.visitSynapse(this); }
}
