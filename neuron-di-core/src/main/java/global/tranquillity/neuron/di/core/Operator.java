package global.tranquillity.neuron.di.core;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public interface Operator<E> {

    default List<E> collect() {
        return foldLeft(new LinkedList<>(), (ms, m) -> { ms.add(m); return ms; });
    }

    <V> V foldLeft(V initialValue, BiFunction<V, E, V> accumulator);
}
