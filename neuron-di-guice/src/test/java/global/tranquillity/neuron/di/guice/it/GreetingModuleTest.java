package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Module;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.core.junit.NeuronRunner;
import global.tranquillity.neuron.di.guice.NeuronModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Neuron
@RunWith(NeuronRunner.class)
public class GreetingModuleTest implements ModuleTest {

    @Override
    public Module module() {
        return new NeuronModule() {

            @Override
            protected void configure() {
                bindNeuron(Greeting.class).in(Singleton.class);
                bind(Formatter.class).to(RealFormatter.class);
                bindConstantNamed("format").to("Hello %s!");
            }
        };
    }

    @Test
    public void testModule() {
        final Greeting greeting = getInstance(Greeting.class);
        assertThat(getInstance(Greeting.class), is(sameInstance(greeting)));
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(sameInstance(greeting.formatter())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
