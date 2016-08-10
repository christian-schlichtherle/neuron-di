package org.neuron_di.it;

import org.junit.Test;
import org.neuron_di.api.CachingStrategy;
import org.neuron_di.api.Neuron;
import org.neuron_di.api.Synapse;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CachingStrategyTest implements NeuronTestMixin {

    @Test
    public void testDisabledCaching() {
        final TestNeuron neuron = make(DisabledCachingNeuron.class);
        assertThat(neuron.dependency(), is(not(sameInstance(neuron.dependency()))));
    }

    @Test
    public void testNotThreadSafeCaching() {
        final TestNeuron neuron = make(NotThreadSafeCachingNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Test
    public void testThreadLocalCaching() {
        final TestNeuron neuron = make(ThreadLocalCachingNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Test
    public void testThreadSafeCaching() {
        final TestNeuron neuron = make(ThreadSafeCachingNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Neuron
    static abstract class DisabledCachingNeuron implements TestNeuron {

        @Synapse(cachingStrategy = CachingStrategy.DISABLED)
        public abstract Object dependency();
    }

    @Neuron
    static abstract class NotThreadSafeCachingNeuron implements TestNeuron {

        @Synapse(cachingStrategy = CachingStrategy.NOT_THREAD_SAFE)
        public abstract Object dependency();
    }

    @Neuron
    static abstract class ThreadLocalCachingNeuron implements TestNeuron {

        @Synapse(cachingStrategy = CachingStrategy.THREAD_LOCAL)
        public abstract Object dependency();
    }

    @Neuron
    static abstract class ThreadSafeCachingNeuron implements TestNeuron {

        // This annotation is redundant, but documents the default behavior:
        @Synapse(cachingStrategy = CachingStrategy.THREAD_SAFE)
        public abstract Object dependency();
    }

    interface TestNeuron {

        Object dependency();
    }
}
