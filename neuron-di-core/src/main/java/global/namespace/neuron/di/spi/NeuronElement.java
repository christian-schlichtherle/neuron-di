/*
 * Copyright © 2016 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        if (isParameterless(method)) {
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

    static boolean isParameterless(Method method) {
        return 0 == method.getParameterCount();
    }

    static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    static boolean isCachingEligible(Method method) {
        return isCachingEnabled(method) && isParameterless(method);
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
