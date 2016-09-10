package global.namespace.neuron.di.api.java.test;

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Neuron;
import global.namespace.neuron.di.api.java.Incubator;
import global.namespace.neuron.di.sample.Clock;

import java.util.Date;

@Neuron
interface FixedClockModule extends ClockModule {

    @Caching
    default Clock clock() {
        return () -> new Date(0);
    }
}
