package global.tranquillity.neuron.di.it;

import org.junit.Test;

import static global.tranquillity.neuron.di.core.Incubator.breed;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class BindingIT {

    @Test
    public void testStringBinding() {
        assertThat(breed(String.class), is(equalTo("")));
    }

    @Test
    public void testInterfaceBinding() {
        try {
            breed(HasDependency.class);
            fail();
        } catch (InstantiationError e) {
            assertThat(e.getCause(), is(instanceOf(NoSuchMethodException.class)));
        }
    }
}
