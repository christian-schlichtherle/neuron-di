package org.neuron_di.it;

import org.neuron_di.api.Caching;
import org.neuron_di.api.Neuron;

import static org.neuron_di.api.CachingStrategy.*;

public class CachingStrategyForDefaultMethodsTest extends CachingStrategyTestSuite {

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
    interface NeuronWithDisabledCachingStrategy extends HasDependency {

        @Caching(DISABLED)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency {

        @Caching(NOT_THREAD_SAFE)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency {

        @Caching(THREAD_LOCAL)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency {

        @Caching
        default Object dependency() { return new Object(); }
    }
}
