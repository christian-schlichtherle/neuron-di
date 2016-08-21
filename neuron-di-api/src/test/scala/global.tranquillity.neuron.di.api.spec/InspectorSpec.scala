package global.tranquillity.neuron.di.api.spec

import java.lang.reflect.Method

import global.tranquillity.neuron.di.api._
import global.tranquillity.neuron.di.api.spec.InspectorSpec._
import global.tranquillity.neuron.di.api.test._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.reflect.ClassTag

class InspectorSpec extends FeatureSpec with GivenWhenThen {

  feature("Neurons and synapses can be inspected without actually instantiating and calling them:") {

    info("As a DI framework")
    info("I want to be able to inspect neuron classes and interfaces ahead of time")
    info("so that I can find synapse methods and bind their return type.")

    scenario("Inspecting some neuron class:") {

      Given("a class annotated with @Neuron")
      When("inspecting the class and computing a list of all synapse methods")
      Then("the list should consist of all synapse methods of all super classes and implemented interfaces.")

      synapsesOf[SomeNeuronClass] shouldHaveNames ("a", "b", "c")
    }

    scenario("Inspecting another neuron class:") {

      Given("a class extending a class annotated with @Neuron")
      When("inspecting the subclass and computing a list of all synapse methods")
      Then("the list should consist of all synapse methods of all super classes and implemented interfaces.")

      synapsesOf[AnotherNeuronClass] shouldHaveNames ("a", "b", "c")
    }

    scenario("Inspecting some neuron interface:") {

      Given("some interface annotated with @Neuron")
      When("inspecting the interface and computing a list of all synapse methods")
      Then("the list should consist of all synapse methods of all extended interfaces.")

      synapsesOf[SomeNeuronInterface] shouldHaveNames ("a", "b", "c")
    }

    scenario("Inspecting another interface:") {

      Given("an interface extending an interface annotated with @Neuron")
      When("inspecting the subinterface and computing a list of all synapse methods")
      Then("the list should be empty because the @Neuron annotation is not inherited when applied to interfaces")
      And("so the subinterface is NOT a neuron.")

      synapsesOf[AnotherInterface] shouldHaveNames ()
    }
  }
}

object InspectorSpec {

  case class synapsesOf[T](implicit tag: ClassTag[T]) {

    private def runtimeClass = tag.runtimeClass
    private val synapses = SynapsesVisitor(runtimeClass)
    private def synapseNames = synapses.map(_.getName)

    def shouldHaveNames(expected: String*) {
      synapseNames shouldBe expected.toList
    }
  }

  object SynapsesVisitor extends Visitor[List[Method]] {

    def apply(clazz: Class[_]): List[Method] =
      (Organism inspect clazz).accept(Nil, this).reverse

    def visitSynapse(methods: List[Method], element: SynapseElement): List[Method] =
      element.method :: methods
  }
}
