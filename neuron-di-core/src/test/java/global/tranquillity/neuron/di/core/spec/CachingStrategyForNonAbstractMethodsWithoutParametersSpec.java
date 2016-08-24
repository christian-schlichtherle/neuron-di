package global.tranquillity.neuron.di.core.spec;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.core.test.HasDependency;

import static global.tranquillity.neuron.di.api.CachingStrategy.*;

@SuppressWarnings("WeakerAccess")
public class CachingStrategyForNonAbstractMethodsWithoutParametersSpec extends CachingStrategySpec {

    @Override
    public String subjects() { return "non-abstract methods without parameters"; }

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
    static class NeuronWithDisabledCachingStrategy implements HasDependency<Object> {

        @Caching(DISABLED)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithNotThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching(NOT_THREAD_SAFE)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadLocalCachingStrategy implements HasDependency<Object> {

        @Caching(THREAD_LOCAL)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching
        public Object dependency() { return new Object(); }
    }
}
