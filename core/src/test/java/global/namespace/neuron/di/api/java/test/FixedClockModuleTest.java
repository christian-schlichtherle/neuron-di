package global.namespace.neuron.di.api.java.test;

import global.namespace.neuron.di.api.java.Incubator;
import global.namespace.neuron.di.sample.Clock;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FixedClockModuleTest {

    @Test
    public void testFixedClockModule() {
        final ClockModule module = Incubator.breed(FixedClockModule.class);
        final Clock clock = module.clock();
        assertThat(module.clock(), is(sameInstance(clock)));
        assertThat(clock.now(), is(not(sameInstance(clock.now()))));
        assertThat(clock.now(), is(new Date(0)));
    }

    @Test
    public void testFixedClockModuleWithoutMatchers() {
        final ClockModule module = Incubator.breed(FixedClockModule.class);
        final Clock clock = module.clock();
        assert clock == module.clock();
        assert new Date(0).equals(clock.now());
    }
}
