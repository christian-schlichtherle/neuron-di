package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.CachingStrategy;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static global.tranquillity.neuron.di.api.CachingStrategy.DISABLED;
import static global.tranquillity.neuron.di.core.Inspection.*;

interface NeuronElement extends ClassElement, HasCachingStrategy {

    @Override
    default void accept(Visitor visitor) { visitor.visitNeuron(this); }

    default void traverseMethods(final Visitor visitor) {
        cglibAdapter((superclass, interfaces) -> {
            final List<Method> methods = new ArrayList<>();
            Enhancer.getMethods(superclass, interfaces, methods);
            for (Method method : methods) {
                element(method).accept(visitor);
            }
            return null;
        })
        .apply(runtimeClass());
    }

    default Element element(final Method method) {

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
            final Optional<CachingStrategy> option =
                    cachingStrategyOption(method);
            if (isAbstract(method)) {
                return new RealSynapseElement(
                        option.orElseGet(this::cachingStrategy));
            } else {
                return new RealMethodElement(option.orElse(DISABLED));
            }
        } else {
            return new RealMethodElement(DISABLED);
        }
    }
}
