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

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.api.Neuron;
import global.namespace.neuron.di.sample.Formatter;
import global.namespace.neuron.di.sample.Greeting;
import global.namespace.neuron.di.sample.RealFormatter;

@Neuron
abstract class GreetingModule {

    @Caching
    Greeting greeting() {
        return Incubator
                .stub(Greeting.class)
                .bind(Greeting::formatter).to(this::formatter)
                .breed();
    }

    private Formatter formatter() { return new RealFormatter("Hello %s!"); }
}
