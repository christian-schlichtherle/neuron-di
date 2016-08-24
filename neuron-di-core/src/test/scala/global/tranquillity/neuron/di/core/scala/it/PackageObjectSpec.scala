package global.tranquillity.neuron.di.core.scala.it

import org.scalatest.WordSpec
import global.tranquillity.neuron.di.core.scala.runtimeClassOf

class PackageObjectSpec extends WordSpec {

  "The runtimeClassOf function" should {
    "throw an exception" in {
      intercept[IllegalArgumentException] {
        runtimeClassOf
      }
    }
  }
}
