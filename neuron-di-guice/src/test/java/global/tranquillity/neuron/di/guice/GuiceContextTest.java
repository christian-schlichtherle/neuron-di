package global.tranquillity.neuron.di.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.inject.name.Names.named;
import static org.junit.Assert.assertSame;

public class GuiceContextTest {

    @Test
    public void testGuiceContextDSL() {
        assertInjector(new GuiceContext()
                .injector()
                    .module()
                        .bind(Foo.class).annotatedWith(named("foo")).to(FooImpl.class).end()
                        .bind(Bar.class).to(BarImpl.class).in(Singleton.class).end()
                        .end()
                    .build());
    }

    @Test
    public void testGuiceDSL() {
        assertInjector(Guice.createInjector(new AbstractModule() {
            @Override protected void configure() {
                bind(Foo.class).annotatedWith(named("foo")).to(FooImpl.class);
                bind(Bar.class).to(BarImpl.class).in(Singleton.class);
            }
        }));
    }

    private void assertInjector(final Injector injector) {
        final Bar bar1 = injector.getInstance(Bar.class);
        final Bar bar2 = injector.getInstance(Bar.class);
        assertSame(bar1, bar2);
    }

    private interface Foo { }

    private static class FooImpl implements Foo { }

    private interface Bar { }

    //@Singleton
    private static class BarImpl implements Bar {
        @Inject
        BarImpl(@Named("foo") Foo foo) { }
    }
}
