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
package global.namespace.neuron.di.api.test;

import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.sample.Clock;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClockModuleTest {

    @Test
    public void testClockModule() {
        final ClockModule module = Incubator.breed(ClockModule.class);
        final Clock clock = module.clock();
        assertThat(module.clock(), is(sameInstance(clock)));
        assertThat(clock.now(), is(not(sameInstance(clock.now()))));
        assertThat(clock.now(), is(lessThanOrEqualTo(new Date())));
    }
}