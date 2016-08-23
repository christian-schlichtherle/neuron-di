package global.tranquillity.neuron.di.guice;

import com.google.inject.Binder;
import com.google.inject.Injector;
import global.tranquillity.neuron.di.core.Organism;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Method;

public interface NeuronModule {

    Binder binder();

    default <T> void bindNeuron(Class<T> runtimeClass) {
        binder().bind(runtimeClass).toProvider(neuronProvider(runtimeClass));
    }

    default <T> Provider<T> neuronProvider(final Class<T> runtimeClass) {
        binder().bind(Organism.class)
                .toProvider((Provider<Organism>) Organism::breed)
                .in(Singleton.class);
        return new Provider<T>() {

            final Provider<Injector> injectorProvider = injectorProvider();
            final Provider<Organism> organismProvider = organismProvider();

            @Override
            public T get() {
                return organism().make(runtimeClass, this::makeSynapse);
            }

            Object makeSynapse(Method method) {
                return injector().getInstance(method.getReturnType());
            }

            Organism organism() { return organismProvider.get(); }

            Provider<Organism> organismProvider() {
                return binder().getProvider(Organism.class);
            }

            Injector injector() { return injectorProvider.get(); }

            Provider<Injector> injectorProvider() {
                return binder().getProvider(Injector.class);
            }
        };
    }
}
