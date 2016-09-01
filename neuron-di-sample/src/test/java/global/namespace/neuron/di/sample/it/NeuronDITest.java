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
package global.namespace.neuron.di.sample.it;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.guice.NeuronModule;
import global.namespace.neuron.di.sample.Formatter;
import global.namespace.neuron.di.sample.Greeting;
import global.namespace.neuron.di.sample.RealFormatter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NeuronDITest {

    @Test
    public void testNeuronDIGuiceAPI() {
        final Module module = new NeuronModule() {

            @Override
            protected void configure() {
                bindNeuron(Greeting.class);
                bind(Formatter.class).to(RealFormatter.class);
                bindConstantNamed("format").to("Hello %s!");
            }
        };
        final Injector injector = Guice.createInjector(module);
        final Greeting greeting = injector.getInstance(Greeting.class);
        testGreeting(greeting);
    }

    @Test
    public void testNeuronDIAPI() {
        final Greeting greeting = new Object() {

            Formatter formatter() { return new RealFormatter("Hello %s!"); }

            Greeting greeting() {
                return Incubator
                        .stub(Greeting.class)
                        .bind(Greeting::formatter).to(this::formatter)
                        .breed();
            }
        }.greeting();
        testGreeting(greeting);
    }

    private void testGreeting(final Greeting greeting) {
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(sameInstance(greeting.formatter())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
