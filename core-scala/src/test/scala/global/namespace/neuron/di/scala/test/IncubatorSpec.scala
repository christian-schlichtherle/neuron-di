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
import global.namespace.neuron.di.scala.sample._
import global.namespace.neuron.di.scala.test.IncubatorSpec._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.reflect.ClassTag

class IncubatorSpec extends FeatureSpec with GivenWhenThen {

  feature("In neuron classes, synapse methods get bound eagerly, but dependencies get resolved lazily.") {

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
      Then("an `IllegalStateException` should be thrown because the @Neuron annotation is not inherited when applied to interfaces")
      And("so the class is NOT a neuron.")

      intercept[IllegalStateException] { synapsesOf[AnotherClass] }
    }
  }

  feature("Neurons can get partially stubbed if explicitly requested.") {

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
      }.getMessage should fullyMatch regex
        """Partial stubbing is disabled and no binding is defined for some synapse methods: \[public abstract java\.lang\.Object global\.namespace\.neuron\.di\.scala\.sample\..*HasDependency\.get\(\)\]"""
    }

    scenario("Partial stubbing is enabled:") {

      Given("a generic interface annotated with @Neuron")
      When("breeding an instance")
      And("partial stubbing has been explicitly enabled")
      And("no binding is defined for some synapse methods")
      Then("the incubator should be recursively applied to resolve the dependency.")

      Incubator
        .stub[HasDependency[_]]
        .partial(true)
        .breed
        .get
        .getClass shouldBe classOf[AnyRef]
    }
  }

  feature("There is at most one proxy class for any neuron class or interface and class loader.") {

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
        .bind(_.get).to("Hello world!")
        .breed
      string.get shouldBe "Hello world!"

      val integer = Incubator
        .stub[HasDependency[Int]]
        .bind(_.get).to(1)
        .breed
      integer.get shouldBe 1

      string.getClass should be theSameInstanceAs integer.getClass
    }
  }

  feature("Any Scala trait with a static context can be a @Neuron trait.") {

    info("As a user of Neuron DI")
    info("I want to be able to annotate any Scala trait with the @Neuron annotation")
    info("so that I can take advantage of the binding DSL")
    info("and eventually apply caching, too.")

    scenario("Breeding some trait with only abstract members:") {

      Given("some trait with only abstract members")
      And("no `val` definition or @Caching annotation")
      When("breeding an instance")
      Then("dependencies should not be cached.")

      val neuron = Incubator
        .stub[Trait1]
        .bind(_.method1).to(new String("method1"))
        .breed
      import neuron._
      method1 shouldBe "method1"
      method1 should not be theSameInstanceAs(method1)
    }

    scenario("Breeding some trait with some non-abstract members:") {

      Given("some trait with some non-abstract members")
      And("some `val` definition but no @Caching annotation")
      When("breeding an instance")
      Then("dependencies should be cached.")

      val neuron = Incubator
        .stub[Trait2]
        .bind(_.method1).to(new String("method1"))
        .breed
      import neuron._
      method1 shouldBe "method1"
      method1 should be theSameInstanceAs method1
      method2 shouldBe "method1 + method2"
      method2 should be theSameInstanceAs method2
    }

    scenario("Breeding some trait extending another trait with some non-abstract members:") {

      Given("some trait extending another trait with some non-abstract members")
      And("some @Caching annotation but no `val` definitions")
      When("breeding an instance")
      Then("dependencies should be cached.")

      val neuron = Incubator
        .stub[Trait3]
        .bind(_.method1).to(new String("method1"))
        .breed
      import neuron._
      method1 shouldBe "method1"
      method1 should be theSameInstanceAs method1
      method2 shouldBe "method1 + method2"
      method2 should be theSameInstanceAs method2
      method3 shouldBe "method1 + method2 + method3"
      method3 should be theSameInstanceAs method3
    }
  }

  feature("@Neuron classes or traits can be stubbed using a rich DSL.") {

    scenario("Breeding some trait using a rich DSL:") {

      val neuron = Incubator
        .stub[Trait4]
        .bind(_.method1, "method1")
        .bind(_.method2)("method2")
        .bind(_.method3)(_ => "method3")
        .bind(_.method4).to(4)
        .bind(_.method5).to(_ => 5)
        .breed
      import neuron._
      method1 shouldBe "method1"
      method2 shouldBe "method2"
      method3 shouldBe "method3"
      method4 shouldBe 4
      method5 shouldBe 5
    }
  }

  feature("@Neuron classes or traits can be breeded using the `neuron` compiler macro.") {

    scenario("Starting simple:") {

      val method1 = "method1"
      val method5 = "method5"
      val neuron = Incubator.neuron[Trait5]
      neuron.method1 shouldBe method1
      neuron.method5 shouldBe method5
    }
  }

  feature("@Neuron classes or traits cannot have synapse methods without a return type.") {

    scenario("Breeding some trait with a synapse method without a return type.") {

      Given("some trait with a synapse method without a return type")
      When("breeding an instance")
      Then("an `IllegalStateException` should be thrown because some type is required to return a dependency")

      intercept[IllegalStateException] {
        Incubator
          .stub[Illegal1]
          .bind(_.method()).to(())
          .breed
      }.getMessage shouldBe
        "Method has void return type: public abstract void global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal1.method()"
    }
  }

  feature("@Neuron classes or traits cannot have abstract methods with parameters.") {

    scenario("Breeding some trait with an abstract method with a parameter:") {

      Given("some trait with an abstract method with a parameter")
      When("breeding an instance")
      Then("an IllegalArgument should be thrown.")

      intercept[IllegalArgumentException] { Incubator.breed[Illegal2] }.getMessage shouldBe
        "Cannot stub abstract methods with parameters: public abstract java.lang.String global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal2.method(java.lang.String)"
    }
  }

  feature("@Neuron classes or traits cannot have constructors which depend on synapses.") {

    scenario("Breeding some trait with an eagerly initialized field whose value depends on a synapse:") {

      Given("some trait with an eagerly initialized field whose value depends on a synapse")
      When("breeding an instance")
      Then("an `IllegalStateException` should be thrown because the lazy dependency resolution is initialized only AFTER the constructor call")

      intercept[IllegalStateException] {
        Incubator
          .stub[Illegal3]
          .bind(_.method1).to("method1")
          .breed
      }.getCause shouldBe a[NullPointerException]
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
  trait Trait2 extends Trait1 {

    val method1: String

    lazy val method2: String = method1 + " + method2"
  }

  @Neuron
  trait Trait3 extends Trait2 {

    @Caching(CachingStrategy.THREAD_LOCAL)
    def method3: String = method2 + " + method3"
  }

  @Neuron
  trait Trait4 extends Trait1 {

    def method2: String
    def method3: String
    def method4: Int
    def method5: Int
  }

  @Neuron
  trait Trait5 extends Trait1 {
    def method5: String
  }

  @Neuron
  trait Illegal1 {

    def method(): Unit
  }

  @Neuron
  trait Illegal2 {

    def method(param: String): String
  }

  @Neuron
  trait Illegal3 extends Trait1 {

    val method1: String

    val method2: String = method1 + " + method2"
  }
}
