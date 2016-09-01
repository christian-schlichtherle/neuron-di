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

import global.namespace.neuron.di.api.Caching;
import global.namespace.neuron.di.api.Neuron;
import global.namespace.neuron.di.sample.HasDependency;

import static global.namespace.neuron.di.api.CachingStrategy.*;

public class CachingStrategyForDefaultMethodsWithoutParametersSpec extends CachingStrategySpec {

    @Override
    public String subjects() { return "default methods without parameters"; }

    @Override
    public Class<? extends HasDependency<?>> classWithDisabledCachingStrategy() {
        return NeuronWithDisabledCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithNotThreadSafeCachingStrategy() {
        return NeuronWithNotThreadSafeCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithThreadLocalCachingStrategy() {
        return NeuronWithThreadLocalCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithThreadSafeCachingStrategy() {
        return NeuronWithThreadSafeCachingStrategy.class;
    }

    @Neuron
    interface NeuronWithDisabledCachingStrategy extends HasDependency<Object> {

        @Caching(DISABLED)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithNotThreadSafeCachingStrategy extends HasDependency<Object> {

        @Caching(NOT_THREAD_SAFE)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadLocalCachingStrategy extends HasDependency<Object> {

        @Caching(THREAD_LOCAL)
        default Object dependency() { return new Object(); }
    }

    @Neuron
    interface NeuronWithThreadSafeCachingStrategy extends HasDependency<Object> {

        @Caching
        default Object dependency() { return new Object(); }
    }
}
