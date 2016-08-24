package global.tranquillity.neuron.di.guice.it;

import com.google.inject.AbstractModule;

import javax.inject.Singleton;

import static com.google.inject.name.Names.named;

public class FooBarModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant().annotatedWith(named("one")).to(1);
        bind(Foo.class)
                .annotatedWith(named("impl"))
                .to(FooImpl.class)
                .in(Singleton.class);
        bind(Bar.class).to(BarImpl.class);
    }
}
