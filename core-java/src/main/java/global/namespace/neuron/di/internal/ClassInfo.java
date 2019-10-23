/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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
import global.namespace.neuron.di.java.Neuron;

import java.lang.reflect.Modifier;
import java.util.Optional;

import static global.namespace.neuron.di.internal.Reflection.findAnnotation;

@FunctionalInterface
interface ClassInfo<C> {

    default void assertCanBeProxied() {
        if (!hasStaticContext()) {
            throw new BreedingException("Class must have a static context: " + clazz());
        }
        if (isFinal()) {
            throw new BreedingException("Class must not be final: " + clazz());
        }
        if (!isInterface() && !hasNonPrivateConstructorWithoutParameters()) {
            throw new BreedingException("Class must have a non-private constructor without parameters: " + clazz());
        }
    }

    Class<C> clazz();

    default Optional<CachingStrategy> classCachingStrategy() {
        return findNeuronAnnotation().map(Neuron::cachingStrategy);
    }

    default Optional<Neuron> findNeuronAnnotation() {
        return findAnnotation(Neuron.class).apply(clazz());
    }

    default boolean hasNeuronAnnotation() {
        return findNeuronAnnotation().isPresent();
    }

    default boolean hasNonPrivateConstructorWithoutParameters() {
        try {
            return !Modifier.isPrivate(clazz().getDeclaredConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    default boolean hasStaticContext() {
        return null == clazz().getEnclosingClass() || isInterface() || isStatic();
    }

    default boolean isAbstract() {
        final boolean a = Modifier.isAbstract(modifiers());
        assert !clazz().isInterface() || a;
        return a;
    }

    default boolean isFinal() {
        return Modifier.isFinal(modifiers());
    }

    default boolean isInterface() {
        return clazz().isInterface();
    }

    default boolean isNeuron() {
        return isAbstract() || hasNeuronAnnotation();
    }

    default boolean isStatic() {
        return Modifier.isStatic(modifiers());
    }

    default int modifiers() {
        return clazz().getModifiers();
    }
}
