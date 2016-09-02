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

import com.google.inject.Injector;
import com.google.inject.Module;
import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Neuron;
import global.namespace.neuron.di.api.junit.NeuronJUnitRunner;
import org.junit.runner.RunWith;

import static com.google.inject.Guice.createInjector;

@Neuron
@RunWith(NeuronJUnitRunner.class)
public abstract class ModuleTest {

    protected abstract Module module();

    @Caching
    protected Injector injector() { return createInjector(module()); }

    protected <T> T getInstance(Class<T> runtimeClass) {
        return injector().getInstance(runtimeClass);
    }
}
