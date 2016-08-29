package global.namespace.neuron.di.guice.it;

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Neuron;

@Neuron
public abstract class Greeting {

    public String message() { return formatter().message("Christian"); }

    // This annotation is actually redundant, but documents the default behavior:
    @Caching
    public abstract Formatter formatter();
}
