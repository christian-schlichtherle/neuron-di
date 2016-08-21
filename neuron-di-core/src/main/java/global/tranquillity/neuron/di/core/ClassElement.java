package global.tranquillity.neuron.di.core;

interface ClassElement extends Element {

    Class<?> runtimeClass();

    @Override
    default void accept(Visitor visitor) { visitor.visitClass(this); }
}
