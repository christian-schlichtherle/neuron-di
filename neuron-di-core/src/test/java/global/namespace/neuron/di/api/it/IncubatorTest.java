package global.namespace.neuron.di.api.it;

import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.sample.Counter;
import global.namespace.neuron.di.sample.Metric;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class IncubatorTest {

    @Test
    public void testStubbing() {
        final Counter a = new Counter();
        final Counter b = new Counter();

        final Metric metric = Incubator
                .stub(Metric.class)
                .bind(Metric::a).to(neuron -> a.inc())
                .bind(Metric::b).to(neuron -> b.inc())
                .breed();

        assertThat(metric.a(), is(sameInstance(a)));
        assertThat(metric.b(), is(sameInstance(b)));

        assertThat(metric.a(), is(sameInstance(a)));
        assertThat(metric.b(), is(sameInstance(b)));

        assertThat(a.count, is(1));
        assertThat(b.count, is(2));
    }
}
