package global.tranquillity.neuron.di.core.spec;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.core.test.HasDependency;

import static global.tranquillity.neuron.di.api.CachingStrategy.*;

public class CachingStrategyForDefaultMethodsSpec extends CachingStrategySpec {

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

    @Neuron
    interface NeuronWithDisabledCachingStrategy extends HasDependency<Object> {

        @Caching(DISABLED)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency<Object> {

        @Caching(NOT_THREAD_SAFE)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency<Object> {

        @Caching(THREAD_LOCAL)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency<Object> {

        @Caching
        default Object dependency() { return new Object(); }
    }
}
