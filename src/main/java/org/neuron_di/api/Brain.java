package org.neuron_di.api;

public class Brain {

    public static Brain build() {
        throw new UnsupportedOperationException();
    }

    public <T> T neuron(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }
}
