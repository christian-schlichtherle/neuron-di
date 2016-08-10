package org.neuron_di.it;

import org.neuron_di.api.Brain;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

interface NeuronTestMixin extends BrainFactory {

    Universe universe = new Universe();

    default <T> T make(final Class<T> type) {
        final T instance = brain().make(type);
        assertThat(instance, is(notNullValue()));
        return instance;
    }

    default Brain brain() { return universe.brain(this); }

    // This design is a bit overkill, but it's just used in test code, so be it.
    class Universe {

        private final Map<BrainFactory, Brain> brains =
                Collections.synchronizedMap(new WeakHashMap<>());

        private Universe() { }

        private Brain brain(BrainFactory factory) {
            return brains.computeIfAbsent(factory, BrainFactory::newBrain);
        }
    }
}
