package global.namespace.neuron.di.scala.sample;

public interface MySuperInterface {

    default String foo() {
        return "foo";
    }

    default String bar() {
        return "bar";
    }
}
