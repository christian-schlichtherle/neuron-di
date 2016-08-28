package global.tranquillity.neuron.di.spi;

interface SynapseElement extends MethodElement {

    @Override
    default void accept(Visitor visitor) { visitor.visitSynapse(this); }
}
