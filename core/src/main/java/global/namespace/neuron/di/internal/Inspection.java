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

import global.namespace.neuron.di.java.CachingStrategy;
import global.namespace.neuron.di.java.Neuron;

final class Inspection {

    private Inspection() { }

    static <T> ClassElement<T> of(final Class<T> runtimeClass) {

        class RealClassElement implements ClassElement<T> {

            public Class<T> runtimeClass() { return runtimeClass; }
        }

        final Neuron neuron = runtimeClass.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement
                    extends RealClassElement
                    implements NeuronElement<T> {

                @Override
                public CachingStrategy cachingStrategy() { return neuron.cachingStrategy(); }
            }

            return new RealNeuronElement();
        } else {
            return new RealClassElement();
        }
    }
}
