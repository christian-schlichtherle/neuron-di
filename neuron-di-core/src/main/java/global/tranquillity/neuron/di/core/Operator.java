package global.tranquillity.neuron.di.core;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface Operator<E> extends Consumer<Consumer<E>> {

    default List<E> toList() {
        return foldLeft(new LinkedList<E>(), (es, e) -> { es.add(e); return es; });
    }

    default <V> V foldLeft(final V initialValue, final BiFunction<V, E, V> accumulator) {

        class Accumulator implements Consumer<E> {

            private V value = initialValue;

            @Override
            public void accept(E element) {
                value = accumulator.apply(value, element);
            }
        }

        final Accumulator consumer = new Accumulator();
        accept(consumer);
        return consumer.value;
    }
}
