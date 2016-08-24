package global.tranquillity.neuron.di.guice.scala.it

import global.tranquillity.neuron.di.guice.it._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class FooBarModuleSpec extends WordSpec with ModuleTest {

  "Module sugar" should {
    "provide an alternative DSL for configuring the foo-bar module" in {
      test(classOf[FooBarModuleTestSuite])
    }
  }
}
