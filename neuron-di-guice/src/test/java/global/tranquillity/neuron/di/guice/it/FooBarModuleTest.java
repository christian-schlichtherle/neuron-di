package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Module;
import com.google.inject.name.Names;
import global.tranquillity.neuron.di.api.Neuron;
import global.tranquillity.neuron.di.core.junit.NeuronJUnitRunner;
import global.tranquillity.neuron.di.guice.NeuronModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Neuron
@RunWith(NeuronJUnitRunner.class)
public class FooBarModuleTest implements ModuleTest {

    @Override
    public Module module() {
        return new NeuronModule() {

            @Override
            protected void configure() {
                bindConstantNamed("one").to(1);
                bind(Foo.class)
                        .annotatedWith(Names.named("impl"))
                        .to(FooImpl.class)
                        .in(Singleton.class);
                bind(Bar.class).to(BarImpl.class);
            }
        };
    }

    @Test
    public void testModule() {
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
