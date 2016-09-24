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

import global.namespace.neuron.di.java.Incubator.breed
import global.namespace.neuron.di.java.sample.HasDependency
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

abstract class CachingStrategySpec extends FeatureSpec with GivenWhenThen {

  feature(s"Developers can configure different caching strategies for $subjects:") {

    info("As a developer")
    info(s"I want to configure the caching strategy for $subjects")
    info(s"by annotating the $subjects with @Caching")
    info("or annotating the neuron class with @Neuron.")

    scenario("Applying the DISABLED caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is DISABLED")

      val neuron = breed(classWithDisabledCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numDependenciesPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be ${collect.numDependencies}.")

      dependencies should have size collect.numDependencies
    }

    scenario("Applying the NOT_THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is NOT_THREAD_SAFE")

      val neuron = breed(classWithNotThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numDependenciesPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be less than or equal to ${collect.numThreads}.")

      dependencies.size should be <= collect.numThreads
    }

    scenario("Applying the THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_SAFE")

      val neuron = breed(classWithThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numDependenciesPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the dependency set should contain exactly one dependency.")

      dependencies shouldBe Set(neuron.get)
    }

    scenario("Applying the THREAD_LOCAL caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_LOCAL")

      val neuron = breed(classWithThreadLocalCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numDependenciesPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be ${collect.numThreads}.")

      dependencies should have size collect.numThreads
    }
  }

  private val collect = new ConcurrentDependencyCollector

  protected def subjects: String

  protected def classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]]
}
