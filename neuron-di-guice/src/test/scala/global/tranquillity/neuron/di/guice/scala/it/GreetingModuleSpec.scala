package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.tranquillity.neuron.di.guice.it._
import global.tranquillity.neuron.di.guice.scala._
import org.scalatest.WordSpec

class GreetingModuleSpec extends WordSpec with GreetingModuleTestMixin {

  "Guice" should {
    "support Neuron bindings" in {
      testGreetingModule(new NeuronModule {

        def configure() {
          bindNeuronClass[Greeting].inScope[Singleton]
          bindClass[Formatter].toClass[RealFormatter]
          bindConstantNamed("format").to("Hello %s!")
        }
      })
    }
  }
}
