/**
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

@SuppressWarnings("WeakerAccess")
public class CachingStrategyForNonAbstractMethodsWithoutParametersSpec extends CachingStrategySpec {

    @Override
    public String subjects() { return "non-abstract methods without parameters"; }

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
    static class NeuronWithDisabledCachingStrategy implements HasDependency<Object> {

        @Caching(DISABLED)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithNotThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching(NOT_THREAD_SAFE)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadLocalCachingStrategy implements HasDependency<Object> {

        @Caching(THREAD_LOCAL)
        public Object dependency() { return new Object(); }
    }

    @Neuron
    static class NeuronWithThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching
        public Object dependency() { return new Object(); }
    }
}
