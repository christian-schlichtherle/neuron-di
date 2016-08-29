package global.namespace.neuron.di.spi;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public class IncubatorImpl {

    private IncubatorImpl() { }

    public static <T> T breed(Class<T> runtimeClass,
                              Function<Method, Supplier<?>> binder) {
        throw new NoClassDefFoundError();
    }
}
