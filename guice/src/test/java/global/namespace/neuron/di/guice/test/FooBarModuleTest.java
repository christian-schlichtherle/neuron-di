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
package global.namespace.neuron.di.guice.test;

import com.google.inject.Module;
import global.namespace.neuron.di.guice.sample.Bar;
import global.namespace.neuron.di.guice.sample.BarImpl;
import global.namespace.neuron.di.guice.sample.FooImpl;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FooBarModuleTest extends ModuleTest {

    @Override
    protected Module module() { return new FooBarModule(); }

    @Test
    public void testFooBarModule() {
        final Bar bar1 = getInstance(Bar.class);
        final Bar bar2 = getInstance(Bar.class);
        assertThat(bar1, is(not(sameInstance(bar2))));
        assertThat(bar1, is(instanceOf(BarImpl.class)));
        assertThat(bar2, is(instanceOf(BarImpl.class)));
        assertThat(bar1.foo(), is(instanceOf(FooImpl.class)));
        assertThat(bar1.foo(), is(sameInstance(bar2.foo())));
        assertThat(bar1.foo().i(), is(1));
    }
}
