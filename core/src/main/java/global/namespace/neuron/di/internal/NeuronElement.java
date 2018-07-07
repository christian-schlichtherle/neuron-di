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
package global.namespace.neuron.di.internal;

import global.namespace.neuron.di.java.BreedingException;
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.CachingStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static global.namespace.neuron.di.java.CachingStrategy.DISABLED;
import static java.util.Optional.ofNullable;

interface NeuronElement<N> extends ClassElement<N>, HasCachingStrategy {

    @Override
    default void accept(Visitor<N> visitor) { visitor.visitNeuron(this); }

    default MethodElement<N> element(final Method method) {

        class MethodBase {

            CachingStrategy cachingStrategy;

            public CachingStrategy cachingStrategy() { return cachingStrategy; }

            public Method method() { return method; }
        }

        class RealSynapseElement extends MethodBase implements SynapseElement<N> {

            private RealSynapseElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        class RealMethodElement extends MethodBase implements MethodElement<N> {

            private RealMethodElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        final Optional<CachingStrategy> declaredCachingStrategy = declaredCachingStrategy(method);
        if (hasParameters(method)) {
            if (declaredCachingStrategy.isPresent()) {
                throw new BreedingException("A caching method must not have parameters: " + method);
            }
            if (isAbstract(method)) {
                throw new BreedingException("Cannot bind abstract methods with parameters: " + method);
            } else {
                return new RealMethodElement(DISABLED);
            }
        } else {
            if (isAbstract(method)) {
                if (isVoid(method)) {
                    throw new BreedingException("A synapse method must have a return value: " + method);
                } else {
                    return new RealSynapseElement(declaredCachingStrategy.orElseGet(this::cachingStrategy));
                }
            } else {
                return new RealMethodElement(declaredCachingStrategy.orElse(DISABLED));
            }
        }
    }

    static Optional<CachingStrategy> declaredCachingStrategy(Method method) {
        return ofNullable(method.getDeclaredAnnotation(Caching.class)).map(Caching::value);
    }

    static boolean hasParameters(Method method) { return 0 != method.getParameterCount(); }

    static boolean isAbstract(Method method) { return Modifier.isAbstract(method.getModifiers()); }

    static boolean isVoid(Method method) { return Void.TYPE == method.getReturnType(); }
}
