package org.neuron_di.it;

import org.junit.Test;
import org.neuron_di.api.Brain;
import org.neuron_di.api.CachingStrategy;
import org.neuron_di.api.Neuron;
import org.neuron_di.api.Synapse;

import javax.inject.Singleton;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeuronTest {

    private final Brain brain = Brain.build();

    @Test
    public void testInjection() { neuron(Greeter.class).greet(); }

    @Test
    public void testScope() {
        final Greeter g1 = neuron(Greeter.class);
        final Greeter g2 = neuron(Greeter.class);
        assertThat(g2, is(not(sameInstance(g1))));
    }

    @Test
    public void testSingletonScope() {
        final Greeter g1 = neuron(SingletonGreeter.class);
        final Greeter g2 = neuron(SingletonGreeter.class);
        assertThat(g2, is(sameInstance(g1)));
    }

    private <T> T neuron(Class<T> clazz) { return brain.neuron(clazz); }

    @Singleton
    // This annotation is redundant, but documents the default behavior:
    @Neuron
    static abstract class SingletonGreeter extends Greeter {
    }

    @Neuron
    static abstract class Greeter {

        // This annotation is redundant, but documents the default behavior:
        @Synapse(caching = CachingStrategy.THREAD_SAFE)
        abstract Greeting greeting();

        void greet() { System.out.println(greeting().message()); }
    }

    @SuppressWarnings("WeakerAccess")
    static class Greeting {
        String message() { return "Hello world!"; }
    }
}
