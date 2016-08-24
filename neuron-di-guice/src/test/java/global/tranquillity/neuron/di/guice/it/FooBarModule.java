package global.tranquillity.neuron.di.guice.it;

import global.tranquillity.neuron.di.guice.NeuronModule;

import javax.inject.Singleton;

import static com.google.inject.name.Names.named;

class FooBarModule extends NeuronModule {

    @Override
    protected void configure() {
        bindConstantNamed("one").to(1);
        bind(Foo.class)
                .annotatedWith(named("impl"))
                .to(FooImpl.class)
                .in(Singleton.class);
        bind(Bar.class).to(BarImpl.class);
    }
}
