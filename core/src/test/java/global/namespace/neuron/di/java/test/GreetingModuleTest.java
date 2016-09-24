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
package global.namespace.neuron.di.java.test;

import global.namespace.neuron.di.java.Incubator;
import global.namespace.neuron.di.java.sample.Greeting;
import global.namespace.neuron.di.java.sample.GreetingModule;
import global.namespace.neuron.di.java.sample.RealFormatter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GreetingModuleTest {

    @Test
    public void testGreetingModule() {
        final GreetingModule module = Incubator.breed(GreetingModule.class);
        final Greeting greeting = module.greeting();
        assertThat(module.greeting(), is(sameInstance(greeting)));
        assertThat(greeting.formatter(), is(instanceOf(RealFormatter.class)));
        assertThat(greeting.formatter(), is(not(sameInstance(greeting.formatter()))));
        assertThat(greeting.message(), is(sameInstance(greeting.message())));
        assertThat(greeting.message(), is("Hello Christian!"));
    }
}
