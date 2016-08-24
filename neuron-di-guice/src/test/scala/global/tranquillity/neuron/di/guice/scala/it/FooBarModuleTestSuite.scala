package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.tranquillity.neuron.di.api.Neuron
import global.tranquillity.neuron.di.guice.it.{Bar, BarImpl, Foo, FooImpl, FooBarModuleTestSuite => jFooBarModuleTestSuite}
import global.tranquillity.neuron.di.guice.scala.{NeuronModule, _}

@Neuron
trait FooBarModuleTestSuite extends jFooBarModuleTestSuite {

  def module = new NeuronModule {

    def configure() {
      bindConstantNamed("one").to(1)
      bindClass[Foo]
        .named("impl")
        .toClass[FooImpl]
        .inScope[Singleton]
      bindClass[Bar].toClass[BarImpl]
    }
  }
}
