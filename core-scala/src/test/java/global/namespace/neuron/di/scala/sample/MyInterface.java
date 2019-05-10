package global.namespace.neuron.di.scala.sample;

public interface MyInterface extends MySuperInterface {

    default String fooBar() {
        return foo() + ", " + bar();
    }

    default String baz() {
        return "baz";
    }
}
