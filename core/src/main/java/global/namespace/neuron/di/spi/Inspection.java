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

import global.namespace.neuron.di.api.CachingStrategy;
import global.namespace.neuron.di.api.Neuron;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

class Inspection {

    private final Class<?> runtimeClass;

    private Inspection(final Class<?> runtimeClass) {
        this.runtimeClass = runtimeClass;
    }

    static Inspection of(Class<?> runtimeClass) {
        return new Inspection(runtimeClass);
    }

    Collection<Method> synapses() {
        final LinkedList<Method> synapses = new LinkedList<>();
        element().accept(new Visitor() {
            @Override
            public void visitSynapse(SynapseElement element) {
                synapses.add(element.method());
            }
        });
        return synapses;
    }

    void accept(Visitor visitor) { element().accept(visitor); }

    private Element element() {

        class RealClassElement implements ClassElement {

            public Class<?> runtimeClass() { return runtimeClass; }
        }

        final Neuron neuron = runtimeClass.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement
                    extends RealClassElement
                    implements NeuronElement {

                @Override
                public CachingStrategy cachingStrategy() {
                    return neuron.cachingStrategy();
                }
            }

            return new RealNeuronElement();
        } else {
            return new RealClassElement();
        }
    }
}
