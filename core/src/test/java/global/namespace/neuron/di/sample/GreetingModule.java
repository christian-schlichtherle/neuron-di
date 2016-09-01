package global.namespace.neuron.di.sample;

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.api.Neuron;

@Neuron
public class GreetingModule {

    @Caching
    public Greeting greeting() {
        return Incubator
                .stub(Greeting.class)
                .bind(Greeting::formatter).to(this::formatter)
                .breed();
    }

    private Formatter formatter() { return new RealFormatter("Hello %s!"); }
}
