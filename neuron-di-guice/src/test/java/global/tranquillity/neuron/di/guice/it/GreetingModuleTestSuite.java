package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Module;
import global.tranquillity.neuron.di.api.Neuron;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Neuron
public interface GreetingModuleTestSuite extends ModuleTestSuite {

    @Override
    default Module module() { return new GreetingModule(); }

    default void test() {
        final Greeting greeting = getInstance(Greeting.class);
        assertThat(getInstance(Greeting.class), is(sameInstance(greeting)));
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(sameInstance(greeting.formatter())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
