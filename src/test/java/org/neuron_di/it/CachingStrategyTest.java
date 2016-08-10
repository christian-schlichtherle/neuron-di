package org.neuron_di.it;

import org.junit.Test;
import static org.neuron_di.api.CachingStrategy.*;
import org.neuron_di.api.Neuron;
import org.neuron_di.api.Synapse;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CachingStrategyTest implements NeuronTestMixin {

    @Test
    public void testDisabledCaching() {
        final HasDependency neuron = make(DisabledCachingStrategyNeuron.class);
        assertThat(neuron.dependency(), is(not(sameInstance(neuron.dependency()))));
    }

    @Test
    public void testNotThreadSafeCaching() {
        final HasDependency neuron = make(NotThreadSafeCachingStrategyNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Test
    public void testThreadLocalCaching() {
        final HasDependency neuron = make(ThreadLocalCachingStrategyNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Test
    public void testThreadSafeCaching() {
        final HasDependency neuron = make(ThreadSafeCachingStrategyNeuron.class);
        // TODO: This is too simplistic! Use a cyclic barrier to test this.
        assertThat(neuron.dependency(), is(sameInstance(neuron.dependency())));
    }

    @Neuron(cachingStrategy = DISABLED)
    interface DisabledCachingStrategyNeuron extends HasDependency { }

    @Neuron(cachingStrategy = NOT_THREAD_SAFE)
    interface NotThreadSafeCachingStrategyNeuron extends HasDependency { }

    @Neuron(cachingStrategy = THREAD_LOCAL)
    interface ThreadLocalCachingStrategyNeuron extends HasDependency { }

    @Neuron
    static abstract class ThreadSafeCachingStrategyNeuron implements HasDependency {

        // This annotation is redundant, but documents the default behavior:
        @Synapse(cachingStrategy = THREAD_SAFE)
        public abstract Object dependency();
    }

    interface HasDependency {

        Object dependency();
    }
}
