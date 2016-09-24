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

import global.namespace.neuron.di.guice.java.sample.Formatter;
import global.namespace.neuron.di.guice.java.sample.Greeting;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class GreetingModuleTest extends ModuleTest {

    @Test
    public void testGreeting() {
        final Greeting greeting = getInstance(Greeting.class);
        assertThat(getInstance(Greeting.class), is(sameInstance(greeting)));
        assertThat(greeting.message("world"), is("Hello world!"));
    }

    @Test
    public void testFormatter() {
        final Formatter formatter = getInstance(Formatter.class);
        assertThat(getInstance(Formatter.class), is(not(sameInstance(formatter))));
        assertThat(formatter.format("world"), is("Hello world!"));
    }
}
