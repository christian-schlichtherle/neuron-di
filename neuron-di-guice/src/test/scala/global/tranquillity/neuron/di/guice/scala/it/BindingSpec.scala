package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import com.google.inject.Guice
import global.tranquillity.neuron.di.guice.it._
import global.tranquillity.neuron.di.guice.scala.InjectorSugar._
import global.tranquillity.neuron.di.guice.scala.ModuleSugar
import org.scalatest.Matchers._
import org.scalatest.{GivenWhenThen, WordSpec}

class BindingSpec extends WordSpec with GivenWhenThen {

  "Guice should support Neuron bindings" in {

    Given("a Guice injector with a Neuron binding")

    val injector = Guice.createInjector(new ModuleSugar {
      def configure() {
        bindNeuronClass[Greeting].inScope[Singleton]
        bindClass[Formatter].toClass[RealFormatter]
        bindConstant.named("format").to("Hello %s!")
      }
    })

    When("injecting a greeting")

    val greeting = injector.getInstanceOf[Greeting]
    import greeting._

    Then("the greeting should be a singleton")

    injector.getInstanceOf[Greeting] shouldBe theSameInstanceAs(greeting)

    And("its formatter should be a real formatter")

    formatter.getClass shouldBe classOf[RealFormatter]

    And("its formatter should be cached although its returned by a method")

    formatter shouldBe theSameInstanceAs(formatter)

    And("its message should be 'Hello Christian!'")

    message shouldBe "Hello Christian!"
  }
}
