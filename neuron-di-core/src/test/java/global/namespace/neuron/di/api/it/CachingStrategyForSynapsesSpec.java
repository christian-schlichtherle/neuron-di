package global.namespace.neuron.di.api.it;

import global.namespace.neuron.di.api.Neuron;
import global.namespace.neuron.di.test.HasDependency;

import static global.namespace.neuron.di.api.CachingStrategy.*;

public class CachingStrategyForSynapsesSpec extends CachingStrategySpec {

    @Override
    public String subjects() { return "synapses"; }

    @Override
    public Class<? extends HasDependency<?>> classWithDisabledCachingStrategy() {
        return NeuronWithDisabledCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithNotThreadSafeCachingStrategy() {
        return NeuronWithNotThreadSafeCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithThreadLocalCachingStrategy() {
        return NeuronWithThreadLocalCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithThreadSafeCachingStrategy() {
        return NeuronWithThreadSafeCachingStrategy.class;
    }

    @Neuron(cachingStrategy = DISABLED)
    interface NeuronWithDisabledCachingStrategy extends HasDependency<Object> { }

    @Neuron(cachingStrategy = NOT_THREAD_SAFE)
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency<Object> { }

    @Neuron(cachingStrategy = THREAD_LOCAL)
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency<Object> { }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency<Object> { }
}
