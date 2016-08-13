package org.neuron_di.it;

import org.neuron_di.api.Neuron;

import static org.neuron_di.api.CachingStrategy.*;

public class CachingStrategyForAbstractMethodsTest extends CachingStrategyTestSuite {

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

    @Neuron(caching = DISABLED)
    interface NeuronWithDisabledCachingStrategy extends HasDependency { }

    @Neuron(caching = NOT_THREAD_SAFE)
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency { }

    @Neuron(caching = THREAD_LOCAL)
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency { }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency { }
}
