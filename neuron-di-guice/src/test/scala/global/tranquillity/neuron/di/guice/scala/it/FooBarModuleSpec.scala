package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Singleton

import global.tranquillity.neuron.di.guice.it._
import global.tranquillity.neuron.di.guice.scala._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class FooBarModuleSpec extends WordSpec with FooBarModuleTestMixin {

  "Module sugar" should {
    "provide an alternative DSL for configuring the foo-bar module" in {
      testFooBarModule(new NeuronModule {

        def configure() {
          bindConstantNamed("one").to(1)
          bindClass[Foo]
            .named("impl")
            .toClass[FooImpl]
            .inScope[Singleton]
          bindClass[Bar].toClass[BarImpl]
        }
      })
    }
  }
}
