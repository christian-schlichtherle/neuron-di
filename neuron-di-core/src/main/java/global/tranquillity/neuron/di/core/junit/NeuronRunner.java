package global.tranquillity.neuron.di.core.junit;

import global.tranquillity.neuron.di.core.Incubator;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class NeuronRunner extends BlockJUnit4ClassRunner {

    public NeuronRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        return Incubator.breed(getTestClass().getJavaClass());
    }
}
