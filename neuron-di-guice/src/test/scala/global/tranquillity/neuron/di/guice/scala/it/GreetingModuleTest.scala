package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.tranquillity.neuron.di.guice.it.{Formatter, Greeting, RealFormatter, GreetingModuleTest => jGreetingModuleTest}
import global.tranquillity.neuron.di.guice.scala.{NeuronModule, _}

class GreetingModuleTest extends jGreetingModuleTest {

  override def module = new NeuronModule {

    def configure() {
      bindNeuronClass[Greeting].inScope[Singleton]
      bindClass[Formatter].toClass[RealFormatter]
      bindConstantNamed("format").to("Hello %s!")
    }
  }
}
