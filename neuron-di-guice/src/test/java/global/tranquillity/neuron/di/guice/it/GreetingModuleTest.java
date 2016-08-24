package global.tranquillity.neuron.di.guice.it;

import org.junit.Test;

public class GreetingModuleTest implements ModuleTest {

    @Test
    public void testGreetingModule() { test(GreetingModuleTestSuite.class); }
}
