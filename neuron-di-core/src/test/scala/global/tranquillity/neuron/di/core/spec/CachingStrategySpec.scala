package global.tranquillity.neuron.di.core.spec

import global.tranquillity.neuron.di.core.Incubator.breed
import global.tranquillity.neuron.di.core.test.HasDependency
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

      dependencies shouldBe Set(neuron.dependency)
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
