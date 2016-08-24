package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Module;
import global.tranquillity.neuron.di.api.Neuron;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Neuron
public interface FooBarModuleTestSuite extends ModuleTestSuite {

    @Override
    default Module module() { return new FooBarModule(); }

    default void test() {
        final Bar bar1 = getInstance(Bar.class);
        final Bar bar2 = getInstance(Bar.class);
        assertThat(bar1, is(not(sameInstance(bar2))));
        assertThat(bar1, is(instanceOf(BarImpl.class)));
        assertThat(bar2, is(instanceOf(BarImpl.class)));
        assertThat(bar1.foo(), is(instanceOf(FooImpl.class)));
        assertThat(bar1.foo(), is(sameInstance(bar2.foo())));
        assertThat(bar1.foo().i(), is(1));
    }
}
