package global.namespace.neuron.di.java.sample;

import java.util.function.UnaryOperator;

public abstract class StringFunction implements UnaryOperator<String> {

    @Override
    public String apply(String s) {
        return "Hello " + s + "!";
    }
}
