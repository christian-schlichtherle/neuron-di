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
import global.namespace.neuron.di.java.CachingStrategy;

import java.lang.reflect.Method;
import java.util.Optional;

import static global.namespace.neuron.di.java.CachingStrategy.DISABLED;

interface ClassElement<C> extends ClassInfo<C>, Element<C> {

    @Override
    default void accept(Visitor<C> visitor) { visitor.visitClass(this); }

    default MethodElement<C> element(final Method method) {

        class Base {

            CachingStrategy cachingStrategy;

            public CachingStrategy cachingStrategy() { return cachingStrategy; }

            public Method method() { return method; }
        }

        class RealSynapseElement extends Base implements SynapseElement<C> {

            private RealSynapseElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        class RealMethodElement extends Base implements MethodElement<C> {

            private RealMethodElement(final CachingStrategy cachingStrategy) {
                super.cachingStrategy = cachingStrategy;
            }
        }

        final MethodInfo info = () -> method;
        final Optional<CachingStrategy> declaredCachingStrategy = info.declaredCachingStrategy();
        if (info.isAbstract()) {
            if (info.hasParameters()) {
                throw new BreedingException("A synapse method must not have parameters: " + method);
            } else if (info.isVoid()) {
                throw new BreedingException("A synapse method must have a return value: " + method);
            } else {
                return new RealSynapseElement(declaredCachingStrategy.orElseGet(this::cachingStrategy));
            }
        } else if (declaredCachingStrategy.isPresent()) {
            if (info.hasParameters()) {
                throw new BreedingException("A caching method must not have parameters: " + method);
            } else if (info.isVoid()) {
                throw new BreedingException("A caching method must have a return value: " + method);
            } else {
                return new RealMethodElement(declaredCachingStrategy.get());
            }
        } else {
            return new RealMethodElement(DISABLED);
        }
    }
}
