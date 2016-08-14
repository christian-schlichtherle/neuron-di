package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;

import static global.tranquillity.neuron.di.api.CachingStrategy.*;

@SuppressWarnings("WeakerAccess")
public class CachingStrategyForConcreteMethodsIT extends CachingStrategyITSuite {

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
    static class NeuronWithDisabledCachingStrategy implements HasDependency {

        @Caching(DISABLED)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithNotThreadSafeCachingStrategy implements HasDependency {

        @Caching(NOT_THREAD_SAFE)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadLocalCachingStrategy implements HasDependency {

        @Caching(THREAD_LOCAL)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadSafeCachingStrategy implements HasDependency {

        @Caching
        public Object dependency() { return new Object(); }
    }
}
