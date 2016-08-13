package org.neuron_di.it;

import org.neuron_di.api.Caching;
import org.neuron_di.api.Neuron;

import static org.neuron_di.api.CachingStrategy.*;

public class CachingStrategyForConcreteMethodsTest extends CachingStrategyTestSuite {

    @Override
    Class<? extends HasDependency> classWithDisabledCachingStrategy() {
        return NeuronWithDisabledCachingStrategy.class;
    }

    @Override
    Class<? extends HasDependency> classWithNotThreadSafeCachingStrategy() {
        return NeuronWithNotThreadSafeCachingStrategy.class;
    }

    @Override
    Class<? extends HasDependency> classWithThreadLocalCachingStrategy() {
        return NeuronWithThreadLocalCachingStrategy.class;
    }

    @Override
    Class<? extends HasDependency> classWithThreadSafeCachingStrategy() {
        return NeuronWithThreadSafeCachingStrategy.class;
    }

    @Neuron
    static abstract class NeuronWithDisabledCachingStrategy implements HasDependency {

        @Caching(DISABLED)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static abstract class NeuronWithNotThreadSafeCachingStrategy implements HasDependency {

        @Caching(NOT_THREAD_SAFE)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static abstract class NeuronWithThreadLocalCachingStrategy implements HasDependency {

        @Caching(THREAD_LOCAL)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static abstract class NeuronWithThreadSafeCachingStrategy implements HasDependency {

        @Caching
        public Object dependency() { return new Object(); }
    }
}
