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
    public void testInjection() { make(Greeter.class).greet(); }

    @Test
    public void testUnscopedNeuron() {
        final Greeter g1 = make(Greeter.class);
        final Greeter g2 = make(Greeter.class);
        assertThat(g2, is(not(sameInstance(g1))));
    }

    @Test
    public void testSingletonScopedNeuron() {
        final Greeter g1 = make(SingletonGreeter.class);
        final Greeter g2 = make(SingletonGreeter.class);
        assertThat(g2, is(sameInstance(g1)));
    }

    @Test
    public void testNoProxy() {
        assertThat(make(Greeting.class).getClass(), is(sameInstance(Greeting.class)));
    }

    private <T> T make(final Class<T> type) {
        final T instance = brain.make(type);
        assertThat(instance, is(notNullValue()));
        return instance;
    }

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
