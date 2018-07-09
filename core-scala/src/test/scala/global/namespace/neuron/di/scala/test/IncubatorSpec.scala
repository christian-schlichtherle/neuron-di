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
import java.util.Date
import java.util.function.Supplier

import global.namespace.neuron.di.java.BreedingException
import global.namespace.neuron.di.scala._
import global.namespace.neuron.di.scala.test.IncubatorSpec._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.reflect.ClassTag

class IncubatorSpec extends FeatureSpec with GivenWhenThen {

  feature("In neuron types, synapse methods get bound eagerly, but dependencies get resolved lazily.") {

    info("As a user of Neuron DI")
    info("I want to be able to visit synapse methods when breeding the neuron")
    info("and compute their return value just in time.")

    scenario("Breeding an instance of a neuron class:") {

      Given("a neuron class")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronClass] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding an instance of another neuron class:") {

      Given("a neuron class extending a class")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all super classes and implemented interfaces.")
      And("not yet compute their return values.")

      synapsesOf[AnotherNeuronClass] shouldHaveNames ("now", "a", "b", "c")
    }

    scenario("Breeding an instance of a neuron interface:") {

      Given("a neuron interface")
      When("breeding an instance")
      Then("the incubator should visit all synapse methods of all extended interfaces.")
      And("not yet compute their return values.")

      synapsesOf[SomeNeuronInterface] shouldHaveNames ("a", "b", "c")
    }

    scenario("Breeding an instance of another class:") {

      Given("a class implementing a neuron interface")
      When("breeding an instance")
      Then("a `BreedingException` should be thrown because the @Neuron annotation is not inherited when applied to interfaces")
      And("so the class is NOT a neuron.")

      intercept[BreedingException] {
        synapsesOf[AnotherClass]
      }.getMessage should startWith("Cannot breed an abstract class without a @Neuron annotation.")
    }
  }

  feature("Neurons can get partially bound if and only if explicitly requested.") {

    info("As a user of Neuron DI")
    info("either I want to explicitly enable partial wiring")
    info("or rest assured that I would get an exception otherwise.")

    scenario("Partial binding is disabled:") {

      Given("a generic neuron interface")
      When("breeding an instance")
      And("partial binding has not been explicitly enabled")
      And("no binding is defined for a synapse methods")
      Then("a `BreedingException` should be thrown.")

      intercept[BreedingException] {
        Incubator.wire[HasDependency[_]].breed
      }.getMessage should fullyMatch regex """Partial binding is disabled and no binding is defined for synapse method: .*"""
    }

    scenario("Partial binding is enabled:") {

      Given("a generic neuron interface")
      When("breeding an instance")
      And("partial binding has been explicitly enabled")
      And("no binding is defined for a synapse method")
      Then("the incubator should be recursively applied to resolve the dependency.")

      Incubator
        .wire[HasDependency[_]]
        .partial(true)
        .breed
        .get
        .getClass shouldBe classOf[AnyRef]
    }
  }

  feature("There is at most one proxy class for any neuron type and class loader.") {

    info("As an application architect")
    info("I want to rest assured that Neuron DI scales nicely")
    info("because it will create at most one proxy class per neuron type (and class loader)")
    info("no matter how many instances of them are created.")

    scenario("Breeding an instance of a generic neuron interface:") {

      Given("a generic neuron interface")
      When("breeding two instances wired with different properties of different types")
      Then("the incubator returns two instances of the same proxy class.")

      val string = Incubator
        .wire[HasDependency[String]]
        .bind(_.get).to("Hello world!")
        .breed
      string.get shouldBe "Hello world!"

      val int = Incubator
        .wire[HasDependency[Int]]
        .bind(_.get).to(1)
        .breed
      int.get shouldBe 1

      string.getClass should be theSameInstanceAs int.getClass
    }
  }

  feature("`.bind(methodReference).to(...)` can be called multiple times for the same method reference") {

    info("As a user of Neuron DI")
    info("I want to be able to call `.bind(methodReference).to(...)` multiple times")
    info("for the same synapse reference")
    info("so that the effect of only the last call persists.")

    scenario("Breeding an instance of a generic neuron interface:") {

      Given("a generic neuron interface")
      When("calling `.bind(methodReference).to(...)` multiple times for the same synapse reference")
      Then("the incubator persists the effect of the last call only.")

      val string = Incubator
        .wire[HasDependency[String]]
        .bind(_.get).to("Hello Christian!")
        .bind(_.get).to("Hello world!")
        .breed
      string.get shouldBe "Hello world!"
    }
  }

  feature("`.breed` can be called multiple times") {

    info("As a user of Neuron DI")
    info("I want to be able to call `.breed` multiple times")
    info("so that different neurons with different bindings can be created in one go.")

    scenario("Breeding an instance of a generic neuron interface:") {

      Given("a generic neuron interface")
      When("calling `.breed` multiple times with different bindings")
      Then("the incubator should create a different neuron reflecting the respective binding each time.")

      var builder = Incubator.wire[HasDependency[String]]
      val helloWorld = builder.bind(_.get).to("Hello world!").breed
      val helloChristian = builder.bind(_.get).to("Hello Christian!").breed

      helloWorld should not be theSameInstanceAs(helloChristian)
      helloWorld.get shouldBe "Hello world!"
      helloChristian.get shouldBe "Hello Christian!"
    }
  }

  feature("Any Scala trait with a static context can be a neuron trait.") {

    info("As a user of Neuron DI")
    info("I want to be able to annotate any Scala trait with the @Neuron annotation")
    info("so that I can take advantage of the binding DSL")
    info("and eventually apply caching, too.")

    scenario("Breeding an instance of a neuron trait with only abstract members:") {

      Given("a neuron trait with only abstract members")
      And("no `val` definition or @Caching annotation")
      When("breeding an instance")
      Then("dependencies should not be cached.")

      val neuron = Incubator
        .wire[Trait1]
        .bind(_.method1).to(new String("method1"))
        .breed
      import neuron._
      method1 shouldBe "method1"
      method1 shouldNot be theSameInstanceAs method1
    }

    scenario("Breeding an instance of a neuron trait with non-abstract members:") {

      Given("a neuron trait with non-abstract members")
      And("a `val` definition but no @Caching annotation")
      When("breeding an instance")
      Then("dependencies should be cached.")

      val neuron = Incubator
        .wire[Trait2]
        .bind(_.method1).to(new String("method1"))
        .breed
      import neuron._
      method1 shouldBe "method1"
      method1 should be theSameInstanceAs method1
      method2 shouldBe "method1 + method2"
      method2 should be theSameInstanceAs method2
    }

    scenario("Breeding an instance of a neuron trait extending another trait with non-abstract members:") {

      Given("a neuron trait extending another trait with non-abstract members")
      And("a @Caching annotation but no `val` definitions")
      When("breeding an instance")
      Then("dependencies should be cached.")

      val neuron = Incubator
        .wire[Trait3]
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

    scenario("Breeding an instance of a neuron trait which requires a shim class and is defined in some static context which is not a package:") {

      Given("a neuron trait which requires a shim class")
      And("is defined in some static context which is not a package")
      When("breeding an instance")
      Then("the shim class should be correctly referenced by its binary name in the associated @Shim annotation")

      val neuron = Incubator
        .wire[Object1.Trait1]
        .bind(_.index).to(1)
        .breed
      neuron.nextIndex shouldBe 2
      // The @Shim annotation is not inherited by the proxy class, but we can sniff its trace in the class name.
      neuron.getClass.getName should include("$$shim")
    }
  }

  feature("Neurons can have different bindings.") {

    scenario("Breeding two instances of a neuron trait with different bindings:") {

      Given("a neuron trait with a synapse method")
      When("breeding two instance")
      Then("different bindings for the synapse method should be observed.")

      Incubator
        .wire[Trait1]
        .bind(_.method1).to("one")
        .breed
        .method1 shouldBe "one"

      Incubator
        .wire[Trait1]
        .bind(_.method1).to("two")
        .breed
        .method1 shouldBe "two"
    }
  }

  feature("Neuron types cannot have synapse methods without a return type.") {

    scenario("Breeding an instance of a neuron trait with a synapse method without a return type:") {

      Given("a neuron trait with a synapse method with the return type `Unit`")
      When("breeding an instance")
      Then("a `BreedingException` should be thrown because a type is required to return a dependency")

      intercept[BreedingException] {
        Incubator
          .wire[Illegal1]
          .bind(_.method()).to(())
          .breed
      }.getMessage shouldBe
        "A synapse method must have a return value: public abstract void global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal1.method()"
    }
  }

  feature("Neuron types cannot have abstract methods with parameters.") {

    scenario("Breeding an instance of a neuron trait with an abstract method with a parameter:") {

      Given("a neuron trait with an abstract method with a parameter")
      When("breeding an instance")
      Then("a `BreedingException` should be thrown.")

      intercept[BreedingException] { Incubator.breed[Illegal2] }.getMessage shouldBe
        "A synapse method must not have parameters: public abstract java.lang.String global.namespace.neuron.di.scala.test.IncubatorSpec$Illegal2.method(java.lang.String)"
    }
  }

  feature("Neuron types cannot have constructors which depend on synapses.") {

    scenario("Breeding an instance of a neuron trait with an eagerly initialized field whose value depends on a synapse:") {

      Given("a neuron trait with an eagerly initialized field whose value depends on a synapse")
      When("breeding an instance")
      Then("a `BreedingException` should be thrown because the lazy dependency resolution is initialized only AFTER the constructor call")

      intercept[BreedingException] {
        Incubator
          .wire[Illegal3]
          .bind(_.method1).to("method1")
          .breed
      }.getCause shouldBe a[NullPointerException]
    }
  }

  feature("Any method without parameters and a non-void return type can be bound.") {

    scenario("Breeding an instance of a neuron interface with a greeting method:") {

      Given("a neuron interface with a greeting method")
      When("breeding an instance with a binding for this method")
      Then("the bound expression should override the method body")

      val neuron = Incubator
        .wire[Greeting]
        .bind(_.get).to(new String("Hello Christian!"))
        .breed

      val greeting1 = neuron.get
      greeting1 shouldBe "Hello Christian!"
      val greeting2 = neuron.get
      greeting2 shouldBe greeting1
      greeting2 should not be theSameInstanceAs(greeting1)

      val cachedGreeting1 = neuron.cached
      cachedGreeting1 shouldBe "Hello Christian!"
      val cachedGreeting2 = neuron.cached
      cachedGreeting2 shouldBe theSameInstanceAs(cachedGreeting1)

      val cachedAgainGreeting1 = neuron.cachedAgain
      cachedAgainGreeting1 shouldBe "Hello Christian!"
      val cachedAgainGreeting2 = neuron.cachedAgain
      cachedAgainGreeting2 shouldBe theSameInstanceAs(cachedAgainGreeting1)
    }
  }
}

private object IncubatorSpec {

  case class synapsesOf[T >: Null : ClassTag]() {

    private var synapses = List.empty[Method]

    Incubator.foo[T] { case method: Method =>
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

  object Object1 {

    @Neuron
    trait Trait1 {

      def index: Int
      def nextIndex: Int = index + 1
    }
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

  trait A {

    def a: A = this
  }

  trait B {

    def b: B = this
  }

  trait HasA {

    def a: A
  }

  trait HasB {

    def b: B
  }

  trait C {

    def c: C = this
  }

  trait HasC {

    def c: C
  }

  @Neuron
  trait SomeNeuronInterface extends HasA with HasB with HasC

  @Neuron
  abstract class SomeNeuronClass extends SomeNeuronInterface

  abstract class AnotherNeuronClass extends SomeNeuronClass {

    def now: Date
  }

  abstract class AnotherClass extends SomeNeuronInterface

  @Neuron
  trait HasDependency[T] extends Supplier[T]

  @Neuron
  trait Greeting extends Supplier[String] {

    def get: String = "Hello world!"

    @Caching
    def cached: String = get

    lazy val cachedAgain: String = get
  }
}
