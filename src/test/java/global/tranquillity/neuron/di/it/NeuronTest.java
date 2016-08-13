package global.tranquillity.neuron.di.it;

import org.junit.Test;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.api.Caching;

import javax.inject.Singleton;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeuronTest implements NeuronTestMixin {

    @Test
    public void testInjection() { make(Greeter.class).greet(); }

    @Test
    public void testDefaultScope() {
        final Greeter g1 = make(Greeter.class);
        final Greeter g2 = make(Greeter.class);
        assertThat(g2, is(not(sameInstance(g1))));
    }

    @Test
    public void testSingletonScope() {
        final Greeter g1 = make(SingletonGreeter.class);
        final Greeter g2 = make(SingletonGreeter.class);
        assertThat(g2, is(sameInstance(g1)));
    }

    @Test
    public void testNoProxy() {
        assertThat(make(Greeting.class).getClass(), is(sameInstance(Greeting.class)));
    }

    @Singleton
    // This annotation is redundant, but documents the default behavior:
    @Neuron
    static abstract class SingletonGreeter extends Greeter { }

    @Neuron
    static abstract class Greeter {

        // This annotation is actually redundant, but documents the default behavior:
        @Caching
        abstract Greeting greeting();

        void greet() { System.out.println(greeting().message("Christian")); }
    }

    @SuppressWarnings("WeakerAccess")
    static class Greeting {

        String message(String name) { return String.format("Hello %s!", name); }
    }
}
