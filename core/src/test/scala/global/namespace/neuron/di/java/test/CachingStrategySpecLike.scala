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
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.{AnyFeatureSpec, AnyFeatureSpecLike}
import org.scalatest.matchers.should.Matchers._

trait CachingStrategySpecLike extends AnyFeatureSpecLike with GivenWhenThen { self: AnyFeatureSpec =>

  private val collect = new ConcurrentDependencyCollector

  protected val subjects: String

  protected val classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]]

  protected val classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected val classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected val classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]]

  Feature(s"Developers can configure different caching strategies for $subjects:") {

    info("As a developer")
    info(s"I want to configure the caching strategy for $subjects")
    info(s"by annotating the $subjects with @Caching")
    info("or annotating the neuron class with @Neuron.")

    Scenario("Applying the DISABLED caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is DISABLED")

      val neuron = breed(classWithDisabledCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numInjectionsPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be ${collect.numInjections}.")

      dependencies should have size collect.numInjections
    }

    Scenario("Applying the NOT_THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is NOT_THREAD_SAFE")

      val neuron = breed(classWithNotThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numInjectionsPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be less than or equal to ${collect.numThreads}.")

      dependencies.size should be <= collect.numThreads
    }

    Scenario("Applying the THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_SAFE")

      val neuron = breed(classWithThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numInjectionsPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the dependency set should contain exactly one dependency.")

      dependencies shouldBe Set(neuron.get)
    }

    Scenario("Applying the THREAD_LOCAL caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_LOCAL")

      val neuron = breed(classWithThreadLocalCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collect.numInjectionsPerThread} times in ${collect.numThreads} threads")

      val dependencies = collect dependenciesOf neuron

      Then(s"the size of the dependency set should be ${collect.numThreads}.")

      dependencies should have size collect.numThreads
    }
  }
}
