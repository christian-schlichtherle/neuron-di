package global.tranquillity.neuron.di.core.spec

import global.tranquillity.neuron.di.core.Inspection
import global.tranquillity.neuron.di.core.spec.InspectionSpec._
import global.tranquillity.neuron.di.core.test._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.reflect.ClassTag

class InspectionSpec extends FeatureSpec with GivenWhenThen {

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

object InspectionSpec {

  case class synapsesOf[T](implicit tag: ClassTag[T]) {

    import scala.collection.JavaConversions._

    private def runtimeClass = tag.runtimeClass

    private val synapses = (Inspection of runtimeClass).withSynapses.toList

    private def synapseNames = synapses.map(_.getName)

    def shouldHaveNames(names: String*) { synapseNames shouldBe names }
  }
}
