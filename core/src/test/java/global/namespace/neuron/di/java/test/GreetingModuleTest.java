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
import global.namespace.neuron.di.java.sample.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GreetingModuleTest {

    @Test
    public void testGreeting() {
        final GreetingModule module = Incubator.breed(GreetingModule.class);
        final Greeting greeting = module.greeting();
        assertThat(greeting, is(instanceOf(RealGreeting.class)));
        assertThat(greeting.message("world"), is("Hello world!"));
        assertThat(module.greeting(), is(sameInstance(greeting)));
    }

    @Test
    public void testFormatter() {
        final GreetingModule module = Incubator.breed(GreetingModule.class);
        final Formatter formatter = module.formatter();
        assertThat(formatter, is(instanceOf(RealFormatter.class)));
        assertThat(formatter.format("world"), is("Hello world!"));
        assertThat(module.formatter(), is(not(sameInstance(formatter))));
    }
}
