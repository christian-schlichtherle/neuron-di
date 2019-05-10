package global.namespace.neuron.di.scala.sample;

public interface MyInterface extends MySuperInterface {

    default String bar() {
        return "bar";
    }
}
