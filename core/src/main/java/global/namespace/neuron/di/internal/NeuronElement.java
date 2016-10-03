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

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.CachingStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static global.namespace.neuron.di.java.CachingStrategy.DISABLED;

interface NeuronElement<T> extends ClassElement<T>, HasCachingStrategy {

    @Override
    default void accept(Visitor<T> visitor) { visitor.visitNeuron(this); }

    default MethodElement<T> element(final Method method) {

        class MethodBase {

            CachingStrategy cachingStrategy;

            public CachingStrategy cachingStrategy() { return cachingStrategy; }

            public Method method() { return method; }
        }

        class RealSynapseElement extends MethodBase implements SynapseElement<T> {

            private RealSynapseElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        class RealMethodElement extends MethodBase implements MethodElement<T> {

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
            if (isAbstract(method)) {
                throw new IllegalArgumentException("Cannot stub abstract methods with parameters: " + method);
            } else {
                return new RealMethodElement(DISABLED);
            }
        }
    }

    static boolean isParameterless(Method method) { return 0 == method.getParameterCount(); }

    static boolean isAbstract(Method method) { return Modifier.isAbstract(method.getModifiers()); }

    static Optional<CachingStrategy> declaredCachingStrategy(Method method) {
        return Optional
                .ofNullable(method.getDeclaredAnnotation(Caching.class))
                .map(Caching::value);
    }
}
