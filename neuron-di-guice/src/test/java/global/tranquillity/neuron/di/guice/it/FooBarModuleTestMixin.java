package global.tranquillity.neuron.di.guice.it;

import com.google.inject.Injector;
import com.google.inject.Module;

import static com.google.inject.Guice.createInjector;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface FooBarModuleTestMixin {

    default void testFooBarModule(Module module) {
        testFooBarInjector(createInjector(module));
    }

    default void testFooBarInjector(final Injector injector) {
        final Bar bar1 = injector.getInstance(Bar.class);
        final Bar bar2 = injector.getInstance(Bar.class);
        assertThat(bar1, is(not(sameInstance(bar2))));
        assertThat(bar1, is(instanceOf(BarImpl.class)));
        assertThat(bar2, is(instanceOf(BarImpl.class)));
        assertThat(bar1.foo(), is(instanceOf(FooImpl.class)));
        assertThat(bar1.foo(), is(sameInstance(bar2.foo())));
        assertThat(bar1.foo().i(), is(1));
    }
}
