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

import com.google.inject.TypeLiteral;
import global.namespace.neuron.di.guice.java.NeuronModule;
import global.namespace.neuron.di.guice.sample.NeuronWithQualifiedGenericSynapses;

import java.util.List;

import static com.google.inject.name.Names.named;
import static java.util.Collections.singletonList;

class NeuronWithQualifiedGenericSynapsesModule extends NeuronModule {

    @Override
    protected void configure() {
        bindNeuron(NeuronWithQualifiedGenericSynapses.class);
        TypeLiteral<List<String>> stringListType = new TypeLiteral<List<String>>() { };
        bind(stringListType)
                .annotatedWith(named("foo"))
                .toInstance(singletonList("foo"));
        bind(stringListType)
                .annotatedWith(named("bar"))
                .toInstance(singletonList("bar"));
    }
}