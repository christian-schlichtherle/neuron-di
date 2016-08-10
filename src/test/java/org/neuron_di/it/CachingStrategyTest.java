package org.neuron_di.it;

import org.junit.Test;
import org.neuron_di.api.Neuron;
import org.neuron_di.api.Synapse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.neuron_di.api.CachingStrategy.*;

public class CachingStrategyTest implements NeuronTestMixin {

    @Test
    public void testDisabledCachingStrategy() {
        final HasDependency neuron = make(DisabledCachingStrategyNeuron.class);
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), hasSize(collector.numDependencies()));
    }

    /**
     * Tests {@link org.neuron_di.api.CachingStrategy#NOT_THREAD_SAFE}.
     * This test implements a Monte Carlo algorithm: In rare cases, it may fail.
     * There is no need to change the code then, just repeat the test.
     */
    @Test
    public void testNotThreadSafeCachingStrategy() {
        final HasDependency neuron = make(NotThreadSafeCachingStrategyNeuron.class);
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), is(not(singleton(neuron.dependency()))));
    }

    @Test
    public void testThreadLocalCachingStrategy() throws InterruptedException {
        final HasDependency neuron = make(ThreadLocalCachingStrategyNeuron.class);
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), hasSize(collector.numThreads()));
    }

    @Test
    public void testThreadSafeCachingStrategy() {
        final HasDependency neuron = make(ThreadSafeCachingStrategyNeuron.class);
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), is(singleton(neuron.dependency())));
    }

    private static class ConcurrentDependencyCollector {

        Set<Object> run(final HasDependency neuron) {
            final CyclicBarrier barrier = new CyclicBarrier(numThreads());

            class TestThread extends Thread {

                List<Object> dependencies;

                @Override
                public void run() {
                    dependencies = IntStream
                            .range(0, dependenciesPerThread())
                            .mapToObj(i -> {
                                await(barrier);
                                return neuron.dependency();
                            })
                            .collect(toList());
                }

                Stream<Object> streamDependencies() {
                    return dependencies.stream();
                }
            }

            return IntStream
                    .range(0, numThreads())
                    .mapToObj(i -> new TestThread())
                    .peek(Thread::start)
                    .collect(toList())
                    .stream()
                    .peek(this::join)
                    .flatMap(TestThread::streamDependencies)
                    .collect(toSet());
        }

        private void await(final CyclicBarrier barrier) {
            try {
                barrier.await(10L, TimeUnit.SECONDS);
            } catch (BrokenBarrierException | InterruptedException | TimeoutException e) {
                throw new AssertionError(e);
            }
        }

        private void join(final Thread thread) {
            try {
                thread.join(10_000L);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }

        int numDependencies() { return numThreads() * dependenciesPerThread(); }

        int numThreads() {
            return 10 * Runtime.getRuntime().availableProcessors();
        }

        int dependenciesPerThread() { return 3; }
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
