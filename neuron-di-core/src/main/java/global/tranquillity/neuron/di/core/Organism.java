package global.tranquillity.neuron.di.core;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Deprecated
public class Organism {

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public static Organism breed() { return new Organism(); }

    private Organism() { }

    /**
     * Returns an instance of the given runtime class which will resolve its
     * dependencies lazily by recursively calling this method.
     */
    public <T> T make(Class<T> runtimeClass) {
        return make(runtimeClass, synapse -> make(synapse.getReturnType()));
    }

    /**
     * Returns an instance of the given runtime class which will resolve its
     * dependencies lazily by calling the given function.
     *
     * @param dependency a function which maps a synapse method to its resolved
     *                   dependency.
     */
    public <T> T make(final Class<T> runtimeClass,
                      final Function<Method, ?> dependency) {
        if (runtimeClass.isAnnotationPresent(Singleton.class)) {
            return runtimeClass.cast(singletons.computeIfAbsent(runtimeClass,
                    (clazz) -> Incubator.breed(clazz, dependency)));
        } else {
            return Incubator.breed(runtimeClass, dependency);
        }
    }
}
