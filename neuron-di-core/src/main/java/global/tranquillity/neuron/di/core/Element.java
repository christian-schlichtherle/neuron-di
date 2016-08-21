package global.tranquillity.neuron.di.core;

public interface Element {

    <V> V accept(V value, Visitor<V> visitor);
}
