package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.tranquillity.neuron.di.api.Neuron
import global.tranquillity.neuron.di.guice.it.{Formatter, Greeting, RealFormatter, GreetingModuleTestSuite => jGreetingModuleTestSuite}
import global.tranquillity.neuron.di.guice.scala.{NeuronModule, _}

@Neuron
trait GreetingModuleTestSuite extends jGreetingModuleTestSuite {

  def module = new NeuronModule {

    def configure() {
      bindNeuronClass[Greeting].inScope[Singleton]
      bindClass[Formatter].toClass[RealFormatter]
      bindConstantNamed("format").to("Hello %s!")
    }
  }
}
