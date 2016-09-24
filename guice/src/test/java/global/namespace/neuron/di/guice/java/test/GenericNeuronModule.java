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
package global.namespace.neuron.di.guice.java.test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import global.namespace.neuron.di.guice.java.NeuronModule;
import global.namespace.neuron.di.guice.java.sample.GenericNeuron;

import java.util.List;

import static java.util.Collections.singletonList;

class GenericNeuronModule extends NeuronModule {

    static final Key<GenericNeuron<String>> STRING_NEURON_KEY =
            Key.get(new TypeLiteral<GenericNeuron<String>>() { });

    @Override
    protected void configure() {
        bindNeuron(STRING_NEURON_KEY);
        bind(String.class).toInstance("foo");
        bind(new TypeLiteral<List<String>>() { }).toInstance(singletonList("foo"));
    }
}
