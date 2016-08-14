package global.tranquillity.neuron.di.it;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import global.tranquillity.neuron.di.guice.GuiceContext;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.inject.name.Names.named;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class GuiceContextIT {

    @Test
    public void testGuiceContextDSL() {
        assertInjector(new GuiceContext()
                .injector()
                    .module()
                        .bindConstant().annotatedWith(named("i")).to(1).end()
                        .bind(Foo.class)
                            .annotatedWith(named("foo"))
                            .to(FooImpl.class)
                            .in(Singleton.class)
                        .end()
                        .bind(Bar.class).to(BarImpl.class).end()
                    .end()
                .build());
    }

    @Test
    public void testGuiceDSL() {
        assertInjector(Guice.createInjector(new AbstractModule() {
            @Override protected void configure() {
                bindConstant().annotatedWith(named("i")).to(1);
                bind(Foo.class)
                        .annotatedWith(named("foo"))
                        .to(FooImpl.class)
                        .in(Singleton.class);
                bind(Bar.class).to(BarImpl.class);
            }
        }));
    }

    private void assertInjector(final Injector injector) {
        final Bar bar1 = injector.getInstance(Bar.class);
        final Bar bar2 = injector.getInstance(Bar.class);
        assertNotSame(bar1, bar2);
        assertThat(bar1, is(instanceOf(BarImpl.class)));
        assertThat(bar2, is(instanceOf(BarImpl.class)));
        assertSame(bar1.foo, bar2.foo);
        assertThat(bar1.foo, is(instanceOf(FooImpl.class)));
        assertThat(bar1.foo.i, is(1));
    }

    private abstract static class Foo {

        int i;
    }

    private static class FooImpl extends Foo {

        @Inject
        FooImpl(@Named("i") int i) { this.i = i; }
    }

    private abstract static class Bar {

        Foo foo;
    }

    private static class BarImpl extends Bar {

        @Inject
        BarImpl(@Named("foo") Foo foo) { this.foo = foo; }
    }
}
