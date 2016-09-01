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
package global.namespace.neuron.di.api.it;

import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.sample.Counter;
import global.namespace.neuron.di.sample.Metric;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class IncubatorTest {

    @Test
    public void testStubbing() {
        final Counter a = new Counter();
        final Counter b = new Counter();

        final Metric metric = Incubator
                .stub(Metric.class)
                .bind(Metric::a).to(neuron -> a.inc())
                .bind(Metric::b).to(neuron -> b.inc())
                .breed();

        assertThat(metric.a(), is(sameInstance(a)));
        assertThat(metric.b(), is(sameInstance(b)));

        assertThat(metric.a(), is(sameInstance(a)));
        assertThat(metric.b(), is(sameInstance(b)));

        assertThat(a.count, is(1));
        assertThat(b.count, is(2));
    }
}
