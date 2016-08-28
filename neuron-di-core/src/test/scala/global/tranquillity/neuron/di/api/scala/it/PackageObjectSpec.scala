package global.tranquillity.neuron.di.api.scala.it

import global.tranquillity.neuron.di.api.scala.runtimeClassOf
import org.scalatest.WordSpec

class PackageObjectSpec extends WordSpec {

  "The runtimeClassOf function" should {
    "throw an exception" in {
      intercept[IllegalArgumentException] {
        runtimeClassOf
      }
    }
  }
}
