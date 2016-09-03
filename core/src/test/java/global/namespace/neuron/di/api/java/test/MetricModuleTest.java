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
import global.namespace.neuron.di.sample.Metric;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class MetricModuleTest {

    @Test
    public void testMetricModule() {
        final MetricModule module = Incubator.breed(MetricModule.class);
        final Metric metric = module.metric();
        assertThat(module.metric(), is(sameInstance(metric)));
        assertThat(metric.counter(), is(sameInstance(metric.counter())));
        assertThat(metric.counter().count, is(0));
        assertThat(metric.incrementCounter().count, is(1));
    }
}
