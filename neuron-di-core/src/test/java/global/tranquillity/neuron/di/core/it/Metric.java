package global.tranquillity.neuron.di.core.it;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;

import static global.tranquillity.neuron.di.api.CachingStrategy.DISABLED;

@Neuron
interface Metric {

    Counter a();

    @Caching(DISABLED)
    Counter b();
}
