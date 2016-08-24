package global.tranquillity.neuron.di.guice.spec

import javax.inject.{Inject, Named, Singleton}

import com.google.inject.Guice
import global.tranquillity.neuron.di.api.{Caching, Neuron}
import global.tranquillity.neuron.di.guice.InjectorSugar._
import global.tranquillity.neuron.di.guice.ModuleSugar
import global.tranquillity.neuron.di.guice.spec.BindingSpec._
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

object BindingSpec {

  @Neuron
  abstract class Greeting {

    lazy val message: String = formatter message "Christian"

    // This annotation is actually redundant, but documents the default behavior:
    @Caching
    def formatter: Formatter
  }

  trait Formatter {

    def message(args: String*): String
  }

  class RealFormatter @Inject()(@Named("format") format: String) extends Formatter {

    def message(args: String*): String = String.format(format, args: _*)
  }
}
