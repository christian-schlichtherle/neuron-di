package global.tranquillity.neuron.di.core;

interface Element {

    <V> V accept(V value, Visitor<V> visitor);
}
