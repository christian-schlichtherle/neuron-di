package global.tranquillity.neuron.di.spi.it;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;

import static global.tranquillity.neuron.di.api.CachingStrategy.DISABLED;

@Neuron
public interface Metric {

    Counter a();

    @Caching(DISABLED)
    Counter b();
}
