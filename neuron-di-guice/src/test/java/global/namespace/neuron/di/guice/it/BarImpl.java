package global.namespace.neuron.di.guice.it;

import javax.inject.Inject;
import javax.inject.Named;

public class BarImpl implements Bar {

    private final Foo foo;

    @Inject
    public BarImpl(final @Named("impl") Foo foo) { this.foo = foo; }

    @Override
    public Foo foo() { return foo; }
}
