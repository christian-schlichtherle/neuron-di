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
import global.namespace.neuron.di.java.test.CachingStrategyForSynapseMethodsSpec._
import global.namespace.neuron.di.sample.HasDependency

class CachingStrategyForSynapseMethodsSpec extends CachingStrategySpec {

  def subjects: String = "synapse methods"

  def classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithDisabledCachingStrategy]

  def classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithNotThreadSafeCachingStrategy]

  def classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadSafeCachingStrategy]

  def classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadLocalCachingStrategy]
}

private object CachingStrategyForSynapseMethodsSpec {

  @Neuron
  trait NeuronWithDisabledCachingStrategy extends HasDependency[Any]

  @Neuron(cachingStrategy = NOT_THREAD_SAFE)
  trait NeuronWithNotThreadSafeCachingStrategy extends HasDependency[Any]

  @Neuron(cachingStrategy = THREAD_SAFE)
  trait NeuronWithThreadSafeCachingStrategy extends HasDependency[Any]

  @Neuron(cachingStrategy = THREAD_LOCAL)
  trait NeuronWithThreadLocalCachingStrategy extends HasDependency[Any]
}
