package global.namespace.neuron.di.guice;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.ConstantBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.api.Neuron;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;

@Neuron
public interface BinderLike {

    Binder binder();

    default ConstantBindingBuilder bindConstantNamed(String name) {
        return binder().bindConstant().annotatedWith(named(name));
    }

    default <T> ScopedBindingBuilder bindNeuron(Class<T> runtimeClass) {
        return binder().bind(runtimeClass).toProvider(neuronProvider(runtimeClass));
    }

    default <T> Provider<T> neuronProvider(final Class<T> runtimeClass) {
        return new Provider<T>() {

            final Provider<Injector> injectorProvider =
                    BinderLike.this.binder().getProvider(Injector.class);

            @Override
            public T get() {
                return Incubator.breed(runtimeClass, this::resolve);
            }

            Supplier<Object> resolve(final Method method) {
                final Injector injector = injector();
                final Class<?> returnType = method.getReturnType();
                return () -> injector.getInstance(returnType);
            }

            Injector injector() { return injectorProvider.get(); }
        };
    }
}
