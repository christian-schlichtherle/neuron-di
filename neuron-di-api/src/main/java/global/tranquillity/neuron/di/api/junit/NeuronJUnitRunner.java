package global.tranquillity.neuron.di.api.junit;

import global.tranquillity.neuron.di.api.Incubator;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class NeuronJUnitRunner extends BlockJUnit4ClassRunner {

    public NeuronJUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        return Incubator.breed(getTestClass().getJavaClass());
    }
}
