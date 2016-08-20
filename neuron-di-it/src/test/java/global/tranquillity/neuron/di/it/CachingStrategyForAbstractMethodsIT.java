package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Neuron;

import static global.tranquillity.neuron.di.api.CachingStrategy.*;

public class CachingStrategyForAbstractMethodsIT extends CachingStrategyITSuite {

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

    @Neuron(cachingStrategy = DISABLED)
    interface NeuronWithDisabledCachingStrategy extends HasDependency { }

    @Neuron(cachingStrategy = NOT_THREAD_SAFE)
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency { }

    @Neuron(cachingStrategy = THREAD_LOCAL)
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency { }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency { }
}
