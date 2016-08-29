package global.namespace.neuron.di.guice.it;

import com.google.inject.Injector;
import com.google.inject.Module;
import global.namespace.neuron.di.api.Caching;

import static com.google.inject.Guice.createInjector;

public interface ModuleTest {

    Module module();

    @Caching
    default Injector injector() { return createInjector(module()); }

    default <T> T getInstance(Class<T> runtimeClass) {
        return injector().getInstance(runtimeClass);
    }
}