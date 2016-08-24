package global.tranquillity.neuron.di.guice.it;

import org.junit.Test;

public class FooBarModuleTest implements FooBarModuleTestMixin {

    @Test
    public void testFooBarModule() {
        testFooBarModule(new FooBarModule());
    }
}
