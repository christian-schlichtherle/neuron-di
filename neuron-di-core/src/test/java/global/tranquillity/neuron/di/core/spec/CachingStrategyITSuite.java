package global.tranquillity.neuron.di.core.spec;

import global.tranquillity.neuron.di.core.test.HasDependency;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static global.tranquillity.neuron.di.core.Incubator.breed;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class CachingStrategyITSuite {

    @Test
    public void testDisabledCachingStrategy() {
        final HasDependency neuron = breed(classWithDisabledCachingStrategy());
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), hasSize(collector.numDependencies()));
    }

    abstract Class<? extends HasDependency> classWithDisabledCachingStrategy();

    @Test
    public void testNotThreadSafeCachingStrategy() {
        final HasDependency neuron = breed(classWithNotThreadSafeCachingStrategy());
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), hasSize(lessThanOrEqualTo(collector.numThreads())));
    }

    abstract Class<? extends HasDependency> classWithNotThreadSafeCachingStrategy();

    @Test
    public void testThreadLocalCachingStrategy() throws InterruptedException {
        final HasDependency neuron = breed(classWithThreadLocalCachingStrategy());
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), hasSize(collector.numThreads()));
    }

    abstract Class<? extends HasDependency> classWithThreadLocalCachingStrategy();

    @Test
    public void testThreadSafeCachingStrategy() {
        final HasDependency neuron = breed(classWithThreadSafeCachingStrategy());
        final ConcurrentDependencyCollector collector = new ConcurrentDependencyCollector();
        assertThat(collector.run(neuron), is(singleton(neuron.dependency())));
    }

    abstract Class<? extends HasDependency> classWithThreadSafeCachingStrategy();

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
}
