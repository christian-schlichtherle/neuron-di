package global.namespace.neuron.di.sample;

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Neuron;

import static global.namespace.neuron.di.api.CachingStrategy.DISABLED;

@Neuron
public interface Metric {

    Counter a();

    @Caching(DISABLED)
    Counter b();
}
