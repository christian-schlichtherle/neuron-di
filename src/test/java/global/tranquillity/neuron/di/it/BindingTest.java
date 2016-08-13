package global.tranquillity.neuron.di.it;

import org.junit.Test;

import java.lang.reflect.UndeclaredThrowableException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class BindingTest extends OrganismTestBase {

    @Test
    public void testStringBinding() {
        assertThat(make(String.class), is(equalTo("")));
    }

    @Test
    public void testInterfaceBinding() {
        try {
            make(HasDependency.class);
            fail();
        } catch (UndeclaredThrowableException e) {
            assertThat(e.getCause(), is(instanceOf(NoSuchMethodException.class)));
        }
    }
}
