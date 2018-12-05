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
import global.namespace.neuron.di.java.MethodBinding;
import global.namespace.neuron.di.java.Neuron;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static global.namespace.neuron.di.java.CachingStrategy.DISABLED;

public final class RealBuilder {

    private static final Map<Class<?>, ProxyFactory<?>> factories = Collections.synchronizedMap(new WeakHashMap<>());

    private RealBuilder() { }

    public static <C> C build(Class<C> clazz, MethodBinding binding) {
        return new Visitor<C>() {

            C instance;

            {
                element(clazz).accept(this);
            }

            @Override
            public void visitNeuron(NeuronElement<C> element) { visitClass(element); }

            @SuppressWarnings("unchecked")
            @Override
            public void visitClass(final ClassElement<C> element) {
                assert clazz == element.clazz();
                final ProxyFactory<C> factory = (ProxyFactory<C>) factories
                        .computeIfAbsent(clazz, key -> new ProxyContext<>(element).factory());
                instance = factory.apply(binding);
            }
        }.instance;
    }

    public static <C> C breed(Class<C> clazz, MethodBinding binding) {
        return new Visitor<C>() {

            C instance;

            {
                element(clazz).accept(this);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void visitNeuron(final NeuronElement<C> element) {
                assert clazz == element.clazz();
                final ProxyFactory<C> factory = (ProxyFactory<C>) factories
                        .computeIfAbsent(clazz, key -> new ProxyContext<>(element).factory());
                instance = factory.apply(binding);
            }

            @Override
            public void visitClass(final ClassElement<C> element) {
                assert clazz == element.clazz();
                try {
                    instance = clazz.getDeclaredConstructor().newInstance();
                } catch (NoSuchMethodException e) {
                    throw new BreedingException("Class must have a non-private constructor without parameters.", e);
                } catch (InstantiationException e) {
                    throw new BreedingException("Cannot breed an abstract class without a @Neuron annotation.", e);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BreedingException(e);
                }
            }
        }.instance;
    }

    private static <C> ClassElement<C> element(final Class<C> clazz) {

        class Base {

            public Class<C> clazz() { return clazz; }
        }

        final Neuron neuron = clazz.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement extends Base implements NeuronElement<C> {

                private final CachingStrategy cachingStrategy = neuron.cachingStrategy();

                @Override
                public CachingStrategy cachingStrategy() { return cachingStrategy; }
            }

            return new RealNeuronElement();
        } else {

            class RealClassElement extends Base implements ClassElement<C> {

                @Override
                public CachingStrategy cachingStrategy() { return DISABLED; }
            }

            return new RealClassElement();
        }
    }
}
