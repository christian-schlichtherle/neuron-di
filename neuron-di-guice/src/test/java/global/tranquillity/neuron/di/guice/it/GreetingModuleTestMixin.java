package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Injector;
import com.google.inject.Module;

import static com.google.inject.Guice.createInjector;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public interface GreetingModuleTestMixin {

    default void testGreetingModule(Module module) {
        testGreetingInjector(createInjector(module));
    }

    default void testGreetingInjector(final Injector injector) {
        final Greeting greeting = injector.getInstance(Greeting.class);
        assertThat(injector.getInstance(Greeting.class), is(sameInstance(greeting)));
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(sameInstance(greeting.formatter())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
