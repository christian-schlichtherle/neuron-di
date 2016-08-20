package global.tranquillity.neuron.di.guice.it

import javax.inject.{Inject, Named}

import com.google.inject.Guice
import com.google.inject.name.Names.named
import global.tranquillity.neuron.di.api.Neuron
import global.tranquillity.neuron.di.guice.ModuleSugar
import global.tranquillity.neuron.di.guice.it.BindingIT._
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BindingIT extends WordSpec {

  "bindings should work" in {
    val injector = Guice.createInjector(new ModuleSugar {
      def configure() {
        bindNeuron[Greeter]
        bind_[Greeting].to_[RealGreeting]
        bindConstant.annotatedWith(named("greeting")).to("Hello %s!")
      }
    })
    pending
    injector.getInstance(classOf[Greeter]).greet shouldBe "Hello Christian!"
  }
}

object BindingIT {

  @Neuron
  abstract class Greeter {

    def greet(): String = greeting message "Christian"

    def greeting: Greeting
  }

  @Neuron
  trait Greeting {

    def message(name: String): String
  }

  class RealGreeting @Inject() (@Named("greeting") format: String) extends Greeting {

    def message(name: String): String = String.format(format, name)
  }
}
