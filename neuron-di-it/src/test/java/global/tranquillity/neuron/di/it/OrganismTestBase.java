package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Organism;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

abstract class OrganismTestBase implements Incubator {

    private volatile Organism organism;

    <T> T make(final Class<T> type) {
        final T instance = organism().make(type);
        assertThat(instance, is(notNullValue()));
        return instance;
    }

    private Organism organism() {
        Organism o;
        if (null == (o = organism)) {
            synchronized (this) {
                if (null == (o = organism)) {
                    organism = o = breed();
                }
            }
        }
        return o;
    }
}
