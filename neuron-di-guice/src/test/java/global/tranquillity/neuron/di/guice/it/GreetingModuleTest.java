package global.tranquillity.neuron.di.guice.it;

import org.junit.Test;

public class GreetingModuleTest implements GreetingModuleTestMixin {

    @Test
    public void testGreetingModule() {
        testGreetingModule(new GreetingModule());
    }
}
