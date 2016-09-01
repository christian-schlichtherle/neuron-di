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
package global.namespace.neuron.di.spi

import global.namespace.neuron.di.api.scala._
import global.namespace.neuron.di.spi.InspectionSpec._
import global.namespace.neuron.di.sample.{AnotherInterface, AnotherNeuronClass, SomeNeuronClass, SomeNeuronInterface}
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

  case class synapsesOf[T](implicit ct: ClassTag[T]) {

    import collection.JavaConversions._

    private val synapses = (Inspection of runtimeClassOf(ct)).synapses

    private def synapseNames = synapses.map(_.getName)

    def shouldHaveNames(names: String*) { synapseNames shouldBe names }
  }
}
