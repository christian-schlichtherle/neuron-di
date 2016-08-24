package global.tranquillity.neuron.di.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;

public abstract class NeuronModule
        extends AbstractModule
        implements BinderLike {

    @Override
    public Binder binder() { return super.binder(); }
}
