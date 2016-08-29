package global.namespace.neuron.di.spi;

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.CachingStrategy;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static global.namespace.neuron.di.api.CachingStrategy.DISABLED;

interface NeuronElement extends ClassElement, HasCachingStrategy {

    @Override
    default void accept(Visitor visitor) { visitor.visitNeuron(this); }

    default void traverseMethods(final Visitor visitor) {
        new CglibFunction<>((superclass, interfaces) -> {
            final List<Method> methods = new ArrayList<>();
            Enhancer.getMethods(superclass, interfaces, methods);
            for (Method method : methods) {
                element(method).accept(visitor);
            }
            return null;
        }).apply(runtimeClass());
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

        if (hasNoParameters(method)) {
            final Optional<CachingStrategy> option =
                    declaredCachingStrategy(method);
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

    static boolean hasNoParameters(Method method) {
        return 0 == method.getParameterCount();
    }

    static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    static boolean isCachingEligible(Method method) {
        return isCachingEnabled(method) && hasNoParameters(method);
    }

    static boolean isCachingEnabled(Method method) {
        return declaredCachingStrategy(method)
                .filter(CachingStrategy::isEnabled)
                .isPresent();
    }

    static Optional<CachingStrategy> declaredCachingStrategy(Method method) {
        return Optional
                .ofNullable(method.getAnnotation(Caching.class))
                .map(Caching::value);
    }
}
