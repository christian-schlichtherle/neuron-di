package global.tranquillity.neuron.di.guice;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.ScopedBindingBuilder;
import global.tranquillity.neuron.di.core.Incubator;

import javax.inject.Provider;
import java.lang.reflect.Method;

public interface NeuronModule {

    Binder binder();

    default <T> ScopedBindingBuilder bindNeuron(Class<T> runtimeClass) {
        return binder().bind(runtimeClass).toProvider(neuronProvider(runtimeClass));
    }

    default <T> Provider<T> neuronProvider(final Class<T> runtimeClass) {
        return new Provider<T>() {

            final Provider<Injector> injectorProvider =
                    binder().getProvider(Injector.class);

            @Override
            public T get() {
                return Incubator.breed(runtimeClass, this::dependency);
            }

            Object dependency(Method method) {
                return injector().getInstance(method.getReturnType());
            }

            Injector injector() { return injectorProvider.get(); }
        };
    }
}
