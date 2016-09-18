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
package global.namespace.neuron.di.java.test;

import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;
import global.namespace.neuron.di.sample.HasDependency;

import static global.namespace.neuron.di.java.CachingStrategy.NOT_THREAD_SAFE;
import static global.namespace.neuron.di.java.CachingStrategy.THREAD_LOCAL;

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
    public Class<? extends HasDependency<?>> classWithThreadSafeCachingStrategy() {
        return NeuronWithThreadSafeCachingStrategy.class;
    }

    @Override
    public Class<? extends HasDependency<?>> classWithThreadLocalCachingStrategy() {
        return NeuronWithThreadLocalCachingStrategy.class;
    }

    @Neuron
    static abstract class NeuronWithDisabledCachingStrategy implements HasDependency<Object> { }

    @Neuron
    static abstract class NeuronWithNotThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching(NOT_THREAD_SAFE)
        public abstract Object get();
    }

    @Neuron
    static abstract class NeuronWithThreadSafeCachingStrategy implements HasDependency<Object> {

        @Caching
        public abstract Object get();
    }

    @Neuron
    static abstract class NeuronWithThreadLocalCachingStrategy implements HasDependency<Object> {

        @Caching(THREAD_LOCAL)
        public abstract Object get();
    }
}
