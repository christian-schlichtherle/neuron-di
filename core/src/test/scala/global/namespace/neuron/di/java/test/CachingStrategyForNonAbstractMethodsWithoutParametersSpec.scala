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
package global.namespace.neuron.di.java.test

import global.namespace.neuron.di.java.CachingStrategy._
import global.namespace.neuron.di.java._
import global.namespace.neuron.di.java.test.CachingStrategyForNonAbstractMethodsWithoutParametersSpec._
import global.namespace.neuron.di.java.sample.HasDependency

class CachingStrategyForNonAbstractMethodsWithoutParametersSpec extends CachingStrategySpec {

  def subjects: String = "non-abstract methods without parameters"

  def classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithDisabledCachingStrategy]

  def classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithNotThreadSafeCachingStrategy]

  def classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadSafeCachingStrategy]

  def classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadLocalCachingStrategy]
}

private object CachingStrategyForNonAbstractMethodsWithoutParametersSpec {

  @Neuron
  abstract class NeuronWithDisabledCachingStrategy extends HasDependency[Any]

  @Neuron
  abstract class NeuronWithNotThreadSafeCachingStrategy extends HasDependency[Any] {

    @Caching(NOT_THREAD_SAFE)
    def get: Any
  }

  @Neuron
  abstract class NeuronWithThreadSafeCachingStrategy extends HasDependency[Any] {

    @Caching
    def get: Any
  }

  @Neuron
  abstract class NeuronWithThreadLocalCachingStrategy extends HasDependency[Any] {

    @Caching(THREAD_LOCAL)
    def get: Any
  }
}
