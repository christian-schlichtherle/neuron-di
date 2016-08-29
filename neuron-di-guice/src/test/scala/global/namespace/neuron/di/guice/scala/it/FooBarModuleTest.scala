package global.namespace.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.namespace.neuron.di.guice.it.{Bar, BarImpl, Foo, FooImpl, FooBarModuleTest => jFooBarModuleTest}
import global.namespace.neuron.di.guice.scala.{NeuronModule, _}

class FooBarModuleTest extends jFooBarModuleTest {

  override def module = new NeuronModule {

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
