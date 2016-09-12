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
package global.namespace.neuron.di.scala.test

import java.lang.reflect.Method

import global.namespace.neuron.di.scala._
import global.namespace.neuron.di.scala.test.IncubatorSpec._
import global.namespace.neuron.di.sample._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.reflect.ClassTag

class IncubatorSpec extends FeatureSpec with GivenWhenThen {

  feature("In neuron classes, synapse methods get bound eagerly, but dependencies get resolved lazily:") {

    info("As a user of Neuron DI")
    info("I want to be able to visit synapse methods when breeding the neuron")
    info("and compute their return value just in time.")

    scenario("Breeding some neuron class:") {

      Given("a class annotated with @Neuron")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronClass] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding another neuron class:") {

      Given("a class extending a class annotated with @Neuron")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[AnotherNeuronClass] shouldHaveNames ("now", "a", "b", "c")
    }

    scenario("Breeding some neuron interface:") {

      Given("some interface annotated with @Neuron")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all extended interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronInterface] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding another class:") {

      Given("a class implementing an interface annotated with @Neuron")
      When("breeding an instance")
      Then("the incubator should throw an `InstantiationError` because the @Neuron annotation is not inherited when applied to interfaces")
      And("so the class is NOT a neuron.")

      intercept[InstantiationError] { synapsesOf[AnotherClass] }
    }
  }

  feature("Partial stubbing needs to be explicitly enabled:") {

    info("As a user of Neuron DI")
    info("either I want to explicitly enable partial stubbing")
    info("or rest assured that I would get an exception otherwise.")

    scenario("Partial stubbing is disabled:") {

      Given("a generic interface annotated with @Neuron")
      When("breeding an instance")
      And("partial stubbing has not been explicitly enabled")
      And("no binding is defined for some synapse methods")
      Then("an `IllegalStateException` should be thrown.")

      intercept[IllegalStateException] {
        Incubator.stub[HasDependency[_]].breed
      }.getMessage shouldBe
        "Partial stubbing is disabled and no binding is defined for some synapse methods: [public abstract java.lang.Object global.namespace.neuron.di.sample.HasDependency.dependency()]"
    }

    scenario("Partial stubbing is enabled:") {

      Given("a generic interface annotated with @Neuron")
      When("breeding an instance")
      And("partial stubbing has been explicitly enabled")
      And("no binding is defined for some synapse methods")
      Then("the incubator should be recursively applied to resolve the dependency.")

      Incubator
        .stub[HasDependency[_ <: AnyRef]]
        .partial(true)
        .breed
        .dependency should not be null
    }
  }

  feature("For each neuron class or interface (and class loader), there is at most one proxy class:") {

    info("As an application architect")
    info("I want to rest assured that Neuron DI scales nicely")
    info("because it will create at most one proxy class per neuron class or interface (and class loader)")
    info("no matter how many instances of them are created.")

    scenario("Breeding some generic neuron interface:") {

      Given("a generic interface annotated with @Neuron")
      When("breeding two instances stubbed with different properties of different types")
      Then("the incubator returns two instances of the same proxy class.")

      val string = Incubator
        .stub[HasDependency[String]]
        .bind(_.dependency).to("Hello world!")
        .breed
      string.dependency shouldBe "Hello world!"

      val integer = Incubator
        .stub[HasDependency[Int]]
        .bind(_.dependency).to(1)
        .breed
      integer.dependency shouldBe 1

      string.getClass should be theSameInstanceAs integer.getClass
    }
  }

  feature("@Neuron traits cannot have non-abstract members:") {

    scenario("Breeding some trait without non-abstract methods:") {

      Incubator.breed[Trait1].method1 shouldBe empty
      Incubator.breed[Class1].method1 shouldBe empty
    }

    scenario("Breeding some trait with non-abstract methods:") {

      intercept[UnsupportedOperationException] { Incubator.breed[Trait2] }
      Incubator.breed[Class2].method2 shouldBe "method2"
    }

    scenario("Breeding some trait extending another trait with non-abstract methods:") {

      intercept[UnsupportedOperationException] { Incubator.breed[Trait3] }
      Incubator.breed[Class3].method3 shouldBe empty
    }
  }
}

object IncubatorSpec {

  case class synapsesOf[T <: AnyRef](implicit ct: ClassTag[T]) {

    private var synapses = List.empty[Method]

    Incubator.breed[T] { method: Method =>
      synapses ::= method
      () => throw new AssertionError
    }

    private lazy val synapseNames = synapses.reverse.map(_.getName)

    def shouldHaveNames(names: String*) { synapseNames shouldBe names }
  }

  @Neuron
  trait Trait1 {

    def method1: String
  }

  @Neuron
  trait Trait2 {

    def method2 = "method2"
  }

  @Neuron
  trait Trait3 extends Trait2 {

    def method3: String
  }

  @Neuron
  abstract class Class1 extends Trait1

  @Neuron
  abstract class Class2 extends Trait2

  @Neuron
  abstract class Class3 extends Trait3
}
