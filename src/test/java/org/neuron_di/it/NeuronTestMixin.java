package org.neuron_di.it;

import org.neuron_di.api.Brain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    class Universe {

        private final Map<BrainFactory, Brain> brains = new ConcurrentHashMap<>();

        private Universe() { }

        private Brain brain(BrainFactory factory) {
            return brains.computeIfAbsent(factory, BrainFactory::newBrain);
        }
    }
}
