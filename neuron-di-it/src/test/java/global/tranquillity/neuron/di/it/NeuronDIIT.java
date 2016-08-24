package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Caching;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.core.Incubator;
import org.junit.Test;

import static global.tranquillity.neuron.di.core.Incubator.breed;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeuronDIIT {

    @Test
    public void testInjection() {
        assertThat(greeter().greet(), is("Hello Christian!"));
    }

    @Test
    public void testScope() {
        final Greeter g1 = greeter();
        final Greeter g2 = greeter();
        assertThat(g2, is(not(sameInstance(g1))));
    }

    private Greeter greeter() { return breed(Greeter.class); }

    @Test
    public void testNoProxy() {
        assertThat(breed(Greeting.class).getClass(), is(sameInstance(Greeting.class)));
    }

    @Neuron
    static abstract class Greeter {

        // This annotation is actually redundant, but documents the default behavior:
        @Caching
        abstract Greeting greeting();

        String greet() { return greeting().message("Christian"); }
    }

    @SuppressWarnings("WeakerAccess")
    static class Greeting {

        String message(String name) { return String.format("Hello %s!", name); }
    }
}
