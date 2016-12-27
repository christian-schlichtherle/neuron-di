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

  feature("In @Neuron types, synapse methods get bound eagerly, but dependencies get resolved lazily.") {

    info("As a user of Neuron DI")
    info("I want to be able to visit synapse methods when breeding the neuron")
    info("and compute their return value just in time.")

    scenario("Breeding a neuron class:") {

      Given("a @Neuron class")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronClass] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding another neuron class:") {

      Given("a @Neuron class extending a class")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[AnotherNeuronClass] shouldHaveNames ("now", "a", "b", "c")
    }

    scenario("Breeding a neuron interface:") {

      Given("a @Neuron interface")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all extended interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronInterface] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding another class:") {

      Given("a class implementing a @Neuron interface")
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

      Given("a generic @Neuron interface")
      When("breeding an instance")
      And("partial stubbing has not been explicitly enabled")
      And("no binding is defined for a synapse methods")
      Then("an `IllegalStateException` should be thrown.")

      intercept[IllegalStateException] {
        Incubator.stub[HasDependency[_]].breed
      }.getMessage should fullyMatch regex
        """Partial stubbing is disabled and no binding is defined for some synapse methods: \[public abstract java\.lang\.Object global\.namespace\.neuron\.di\.scala\.sample\..*HasDependency\.get\(\)\]"""
    }

    scenario("Partial stubbing is enabled:") {

      Given("a generic @Neuron interface")
      When("breeding an instance")
      And("partial stubbing has been explicitly enabled")
      And("no binding is defined for a synapse methods")
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

    scenario("Breeding a generic @Neuron interface:") {

      Given("a generic @Neuron interface")
      When("breeding two instances stubbed with different properties of different types")
      Then("the incubator returns two instances of the same proxy class.")

      val string = Incubator
        .stub[HasDependency[String]]
        .bind(_.get).to("Hello world!")
        .breed
      string.get shouldBe "Hello world!"

      val int = Incubator
        .stub[HasDependency[Int]]
        .bind(_.get).to(1)
        .breed
      int.get shouldBe 1

      string.getClass should be theSameInstanceAs int.getClass
    }
  }

  feature("Any Scala trait with a static context can be a @Neuron trait.") {

    info("As a user of Neuron DI")
    info("I want to be able to annotate any Scala trait with the @Neuron annotation")
    info("so that I can take advantage of the binding DSL")
    info("and eventually apply caching, too.")

    scenario("Breeding a @Neuron trait with only abstract members:") {

      Given("a @Neuron trait with only abstract members")
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

    scenario("Breeding a @Neuron trait with a non-abstract members:") {

      Given("a @Neuron trait with a non-abstract members")
      And("a `val` definition but no @Caching annotation")
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

    scenario("Breeding a @Neuron trait extending another trait with a non-abstract members:") {

      Given("a @Neuron trait extending another trait with a non-abstract members")
      And("a @Caching annotation but no `val` definitions")
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

  feature("@Neuron types cannot have synapse methods without a return type.") {

    scenario("Breeding a @Neuron trait with a synapse method without a return type.") {

      Given("a @Neuron trait with a synapse method without a return type")
      When("breeding an instance")
      Then("an `IllegalStateException` should be thrown because a type is required to return a dependency")

      intercept[IllegalStateException] {
        Incubator
          .stub[Illegal1]
          .bind(_.method()).to(())
          .breed
      }.getMessage shouldBe
        "Method has void return type: public abstract void global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal1.method()"
    }
  }

  feature("@Neuron types cannot have abstract methods with parameters.") {

    scenario("Breeding a @Neuron trait with an abstract method with a parameter:") {

      Given("a @Neuron trait with an abstract method with a parameter")
      When("breeding an instance")
      Then("an IllegalArgument should be thrown.")

      intercept[IllegalArgumentException] { Incubator.breed[Illegal2] }.getMessage shouldBe
        "Cannot stub abstract methods with parameters: public abstract java.lang.String global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal2.method(java.lang.String)"
    }
  }

  feature("@Neuron types cannot have constructors which depend on synapses.") {

    scenario("Breeding a @Neuron trait with an eagerly initialized field whose value depends on a synapse:") {

      Given("a @Neuron trait with an eagerly initialized field whose value depends on a synapse")
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

  feature("Dependencies of @Neuron types can be wired using the `neuron` compiler macro.") {

    scenario("HasDependency[String]:") {
      val get = "Hello world!"
      val neuron = Incubator.neuron[HasDependency[String]]
      neuron.get shouldBe get
    }

    scenario("HasDependency[Int]:") {
      val get = 1
      val neuron = Incubator.neuron[HasDependency[Int]]
      neuron.get shouldBe get
    }

    scenario("Trait1:") {
      def method1 = new String("method1")
      val neuron = Incubator.neuron[Trait1]
      neuron.method1 shouldBe method1
      neuron.method1 should not be theSameInstanceAs(neuron.method1)
    }

    scenario("Trait2:") {
      def method1 = new String("method1")
      val neuron = Incubator.neuron[Trait2]
      neuron.method1 shouldBe method1
      neuron.method1 should be theSameInstanceAs neuron.method1
      neuron.method2 shouldBe s"$method1 + method2"
      neuron.method2 should be theSameInstanceAs neuron.method2
    }

    scenario("Trait3:") {
      def method1 = new String("method1")
      val neuron = Incubator.neuron[Trait3]
      neuron.method1 shouldBe method1
      neuron.method1 should be theSameInstanceAs neuron.method1
      neuron.method2 shouldBe s"$method1 + method2"
      neuron.method2 should be theSameInstanceAs neuron.method2
      neuron.method3 shouldBe s"$method1 + method2 + method3"
      neuron.method3 should be theSameInstanceAs neuron.method3
    }

    scenario("Trait4:") {
      val method1 = "method1"
      val method5 = "method5"
      val neuron = Incubator.neuron[Trait4]
      neuron.method1 shouldBe method1
      neuron.method5 shouldBe method5
    }

    scenario("Trait5:") {
      val a = 1
      val b = 2
      val neuron = Incubator.neuron[Trait5[Int, Int]]
      neuron.a shouldBe a
      neuron.b shouldBe b
    }

    scenario("Trait6:") {
      val a = 1
      val b = a.toString
      val neuron = Incubator.neuron[Trait6]
      neuron.a shouldBe a
      neuron.b shouldBe b
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

    def method5: String
  }

  @Neuron
  trait Trait5[A, B] {
    def a: A
    def b: B
  }

  @Neuron
  trait Trait6 extends Trait5[Int, String]

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
