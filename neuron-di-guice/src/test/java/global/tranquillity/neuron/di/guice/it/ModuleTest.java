package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Module;
import global.tranquillity.neuron.di.guice.NeuronModule;

import static com.google.inject.Guice.createInjector;

public interface ModuleTest {

    default void test(final Class<? extends ModuleTestSuite> suiteClass) {
        final Module testModule = new NeuronModule() {

            @Override
            protected void configure() { bindNeuron(suiteClass); }
        };
        createInjector(testModule).getInstance(suiteClass).test();
    }
}
