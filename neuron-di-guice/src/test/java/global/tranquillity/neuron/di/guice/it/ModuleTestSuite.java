package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Injector;
import com.google.inject.Module;
import global.tranquillity.neuron.di.api.Caching;

import static com.google.inject.Guice.createInjector;

public interface ModuleTestSuite {

    Module module();

    @Caching
    default Injector injector() { return createInjector(module()); }

    default <T> T getInstance(Class<T> runtimeClass) {
        return injector().getInstance(runtimeClass);
    }

    void test();
}
