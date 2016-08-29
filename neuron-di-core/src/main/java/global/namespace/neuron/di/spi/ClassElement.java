package global.namespace.neuron.di.spi;

interface ClassElement extends Element {

    Class<?> runtimeClass();

    @Override
    default void accept(Visitor visitor) { visitor.visitClass(this); }
}
