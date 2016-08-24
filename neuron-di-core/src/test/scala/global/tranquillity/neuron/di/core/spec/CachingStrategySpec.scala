package global.tranquillity.neuron.di.core.spec

import global.tranquillity.neuron.di.core.Incubator.breed
import global.tranquillity.neuron.di.core.test.HasDependency
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

abstract class CachingStrategySpec extends FeatureSpec with GivenWhenThen {

  feature("Developers can configure different caching strategies for synapses:") {

    info("As a developer")
    info("I want to configure the caching strategy for dependencies")
    info("by using the @Caching annotation on the synapse method")
    info("or the @Neuron annotation on the neuron class.")

    val collector = new ConcurrentDependencyCollector

    scenario("Applying the DISABLED caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is DISABLED")

      val neuron = breed(classWithDisabledCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collector.numDependenciesPerThread} times in ${collector.numThreads} threads")

      val dependencies = collector runOn neuron

      Then(s"the size of the dependency set should be ${collector.numDependencies}.")

      dependencies should have size collector.numDependencies
    }

    scenario("Applying the NOT_THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is NOT_THREAD_SAFE")

      val neuron = breed(classWithNotThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collector.numDependenciesPerThread} times in ${collector.numThreads} threads")

      val dependencies = collector runOn neuron

      Then(s"the size of the dependency set should be less than or equal to ${collector.numThreads}.")

      dependencies.size should be <= collector.numThreads
    }

    scenario("Applying the THREAD_SAFE caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_SAFE")

      val neuron = breed(classWithThreadSafeCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collector.numDependenciesPerThread} times in ${collector.numThreads} threads")

      val dependencies = collector runOn neuron

      Then(s"the dependency set should contain exactly one dependency.")

      dependencies shouldBe Set(neuron.dependency)
    }

    scenario("Applying the THREAD_LOCAL caching strategy:") {

      Given("A neuron where the caching strategy for its dependency is THREAD_LOCAL")

      val neuron = breed(classWithThreadLocalCachingStrategy)

      When(s"concurrently collecting dependencies injected ${collector.numDependenciesPerThread} times in ${collector.numThreads} threads")

      val dependencies = collector runOn neuron

      Then(s"the size of the dependency set should be ${collector.numThreads}.")

      dependencies should have size collector.numThreads
    }
  }

  protected def classWithDisabledCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithNotThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithThreadSafeCachingStrategy: Class[_ <: HasDependency[_]]

  protected def classWithThreadLocalCachingStrategy: Class[_ <: HasDependency[_]]
}
