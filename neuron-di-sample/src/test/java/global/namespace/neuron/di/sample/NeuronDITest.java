package global.namespace.neuron.di.sample;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.guice.NeuronModule;
import global.namespace.neuron.di.sample.test.Formatter;
import global.namespace.neuron.di.sample.test.Greeting;
import global.namespace.neuron.di.sample.test.RealFormatter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class NeuronDITest {

    @Test
    public void testNeuronDIGuiceAPI() {
        final Module module = new NeuronModule() {

            @Override
            protected void configure() {
                bindNeuron(Greeting.class);
                bind(Formatter.class).to(RealFormatter.class);
                bindConstantNamed("format").to("Hello %s!");
            }
        };
        final Injector injector = Guice.createInjector(module);
        final Greeting greeting = injector.getInstance(Greeting.class);
        testGreeting(greeting);
    }

    @Test
    public void testNeuronDIAPI() {
        final Greeting greeting = new Object() {

            Formatter formatter() { return new RealFormatter("Hello %s!"); }

            Greeting greeting() {
                return Incubator
                        .stub(Greeting.class)
                        .bind(Greeting::formatter).to(this::formatter)
                        .breed();
            }
        }.greeting();
        testGreeting(greeting);
    }

    private void testGreeting(final Greeting greeting) {
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(sameInstance(greeting.formatter())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
