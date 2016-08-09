package org.neuron_di.it;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

public class GuiceTest {

    private final Injector injector = Guice.createInjector(new TestModule());

    @Test
    public void testInjection() { getInstance(Greeter.class).greet(); }

    @Test
    public void testScope() {
        final Greeter g1 = getInstance(Greeter.class);
        final Greeter g2 = getInstance(Greeter.class);
        assertThat(g2, is(not(sameInstance(g1))));
    }

    @Test
    public void testSingletonScope() {
        final Greeter g1 = getInstance(SingletonGreeter.class);
        final Greeter g2 = getInstance(SingletonGreeter.class);
        assertThat(g2, is(sameInstance(g1)));
    }

    private <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() { }
    }

    @Singleton
    private static class SingletonGreeter extends Greeter {

        @Inject
        SingletonGreeter(Greeting greeting) { super(greeting); }
    }

    private static class Greeter {

        final Greeting greeting;

        @Inject
        Greeter(Greeting greeting) { this.greeting = greeting; }

        void greet() { System.out.println(greeting.message()); }
    }

    private static class Greeting {
        String message() { return "Hello world!"; }
    }
}
