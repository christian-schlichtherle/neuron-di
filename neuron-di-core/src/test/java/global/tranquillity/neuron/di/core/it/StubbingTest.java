package global.tranquillity.neuron.di.core.it;

import global.tranquillity.neuron.di.core.Incubator;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

public class StubbingTest {

    @Test
    public void testStubbing() {
        final A a = mock(A.class);
        final B b = mock(B.class);
        final C c = mock(C.class);
        final SomeNeuronClass neuron = Incubator
                .stub(SomeNeuronClass.class)
                .put(HasA::a, ignored -> {
                    System.out.println(a);
                    return a;
                })
                .put(HasB::b, ignored -> {
                    System.out.println(b);
                    return b;
                })
                .put(HasC::c, ignored -> {
                    System.out.println(c);
                    return c;
                })
                .breed();

        assertThat(neuron.a(), is(sameInstance(a)));
        assertThat(neuron.b(), is(sameInstance(b)));
        assertThat(neuron.c(), is(sameInstance(c)));

        assertThat(neuron.a(), is(sameInstance(a)));
        assertThat(neuron.b(), is(sameInstance(b)));
        assertThat(neuron.c(), is(sameInstance(c)));
    }
}
