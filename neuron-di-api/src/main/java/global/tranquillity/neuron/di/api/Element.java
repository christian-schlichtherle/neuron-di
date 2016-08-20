package global.tranquillity.neuron.di.api;

public interface Element {

    <V> V accept(V value, Visitor<V> visitor);
}
