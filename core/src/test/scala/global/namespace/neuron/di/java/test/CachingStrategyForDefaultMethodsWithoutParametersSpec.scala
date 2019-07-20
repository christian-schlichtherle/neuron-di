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
import global.namespace.neuron.di.java.test.CachingStrategyForDefaultMethodsWithoutParametersSpec._
import global.namespace.neuron.di.java.sample.HasDependency
import org.scalatest.FeatureSpec

class CachingStrategyForDefaultMethodsWithoutParametersSpec extends FeatureSpec with CachingStrategySpecLike {

  lazy val subjects: String = "default methods without parameters"

  lazy val classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithDisabledCachingStrategy]

  lazy val classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithNotThreadSafeCachingStrategy]

  lazy val classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadSafeCachingStrategy]

  lazy val classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]] =
    classOf[NeuronWithThreadLocalCachingStrategy]
}

private object CachingStrategyForDefaultMethodsWithoutParametersSpec {

  @Neuron
  trait NeuronWithDisabledCachingStrategy extends HasDependency[Any]

  @Neuron
  trait NeuronWithNotThreadSafeCachingStrategy extends HasDependency[Any] {

    @Caching(NOT_THREAD_SAFE)
    def get: Any
  }

  @Neuron
  trait NeuronWithThreadSafeCachingStrategy extends HasDependency[Any] {

    @Caching
    def get: Any
  }

  @Neuron
  trait NeuronWithThreadLocalCachingStrategy extends HasDependency[Any] {

    @Caching(THREAD_LOCAL)
    def get: Any
  }
}
