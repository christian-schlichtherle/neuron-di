package global.tranquillity.neuron.di.guice.spec

import javax.inject.{Inject, Named}

import com.google.inject.Guice
import global.tranquillity.neuron.di.api.Neuron
import global.tranquillity.neuron.di.guice.ModuleSugar
import global.tranquillity.neuron.di.guice.spec.BindingSpec._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import global.tranquillity.neuron.di.guice.InjectorSugar._

class BindingSpec extends WordSpec {

  "binding neurons should work" in {
    val injector = Guice.createInjector(new ModuleSugar {
      def configure() {
        bindNeuronClass[Greeter]
        bindClass[Greeting].toClass[RealGreeting]
        bindConstant.named("greeting").to("Hello %s!")
      }
    })
    injector.getInstanceOf[Greeter].greet shouldBe "Hello Christian!"
  }
}

object BindingSpec {

  @Neuron
  abstract class Greeter {

    def greet(): String = greeting message "Christian"

    def greeting: Greeting
  }

  trait Greeting {

    def message(name: String): String
  }

  class RealGreeting @Inject() (@Named("greeting") format: String) extends Greeting {

    def message(name: String): String = String.format(format, name)
  }
}
