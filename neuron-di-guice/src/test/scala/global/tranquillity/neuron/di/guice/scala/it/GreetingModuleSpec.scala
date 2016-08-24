package global.tranquillity.neuron.di.guice.scala.it

import global.tranquillity.neuron.di.guice.it._
import org.scalatest.WordSpec

class GreetingModuleSpec extends WordSpec with ModuleTest {

  "Guice" should {
    "support Neuron bindings" in {
      test(classOf[GreetingModuleTestSuite])
    }
  }
}
