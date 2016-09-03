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
package global.namespace.neuron.di.api.java.test;

import global.namespace.neuron.di.api.java.Incubator;
import global.namespace.neuron.di.sample.CounterFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CounterFactoryModuleTest {

    @Test
    public void testCounterFactoryModule() {
        final CounterFactoryModule module = Incubator.breed(CounterFactoryModule.class);
        final CounterFactory factory = module.counterFactory();
        assertThat(module.counterFactory(), is(sameInstance(factory)));
        assertThat(factory.counter(), is(not(sameInstance(factory.counter()))));
    }
}
