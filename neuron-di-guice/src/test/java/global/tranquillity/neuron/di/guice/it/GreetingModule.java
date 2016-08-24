package global.tranquillity.neuron.di.guice.it;

import global.tranquillity.neuron.di.guice.NeuronModule;

import javax.inject.Singleton;

class GreetingModule extends NeuronModule {

    @Override
    protected void configure() {
        bindNeuron(Greeting.class).in(Singleton.class);
        bind(Formatter.class).to(RealFormatter.class);
        bindConstantNamed("format").to("Hello %s!");
    }
}
