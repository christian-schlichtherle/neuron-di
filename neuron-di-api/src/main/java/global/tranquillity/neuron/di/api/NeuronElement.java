package global.tranquillity.neuron.di.api;

import java.lang.reflect.Method;
import java.util.Optional;

import static global.tranquillity.neuron.di.api.CachingStrategy.DISABLED;
import static global.tranquillity.neuron.di.api.Organism.*;

public interface NeuronElement extends ClassElement, HasCachingStrategy {

    default <V> V traverse(V value, final Visitor<V> visitor) {
        for (Method method : clazz().getMethods()) {
            value = inspect(method).accept(value, visitor);
        }
        return value;
    }

    default MethodElement inspect(final Method method) {

        class MethodBase {

            CachingStrategy cachingStrategy;

            public CachingStrategy cachingStrategy() { return cachingStrategy; }

            public Method method() { return method; }
        }

        class RealSynapseElement extends MethodBase implements SynapseElement {

            private RealSynapseElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        class RealMethodElement extends MethodBase implements MethodElement {

            private RealMethodElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        if (isParameterless(method)) {
            final Optional<CachingStrategy> option = cachingStrategyOption(method);
            if (isAbstract(method)) {
                return new RealSynapseElement(
                        option.orElseGet(this::cachingStrategy));
            } else {
                return new RealMethodElement(
                        option.orElse(DISABLED));
            }
        } else {
            return new RealMethodElement(DISABLED);
        }
    }

    @Override
    default <V> V accept(V value, Visitor<V> visitor) {
        return visitor.visitNeuron(value, this);
    }
}
