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

import global.namespace.neuron.di.guice.java.NeuronModule;
import global.namespace.neuron.di.guice.java.sample.Bar;
import global.namespace.neuron.di.guice.java.sample.BarImpl;
import global.namespace.neuron.di.guice.java.sample.Foo;
import global.namespace.neuron.di.guice.java.sample.FooImpl;

import javax.inject.Singleton;

import static com.google.inject.name.Names.named;

class FooBarModule extends NeuronModule {

    @Override
    protected void configure() {
        bindConstantNamed("one").to(1);
        bind(Foo.class)
                .annotatedWith(named("impl"))
                .to(FooImpl.class)
                .in(Singleton.class);
        bind(Bar.class).to(BarImpl.class);
    }
}
