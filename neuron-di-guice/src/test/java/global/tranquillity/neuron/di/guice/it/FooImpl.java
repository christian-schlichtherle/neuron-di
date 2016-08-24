package global.tranquillity.neuron.di.guice.it;

import javax.inject.Inject;
import javax.inject.Named;

public class FooImpl implements Foo {

    private final int i;

    @Inject
    public FooImpl(final @Named("one") int i) { this.i = i; }

    public int i() { return i; }
}
