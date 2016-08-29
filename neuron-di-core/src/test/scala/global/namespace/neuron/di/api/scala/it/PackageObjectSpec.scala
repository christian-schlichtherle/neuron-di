package global.namespace.neuron.di.api.scala.it

import global.namespace.neuron.di.api.scala.runtimeClassOf
import org.scalatest.WordSpec

class PackageObjectSpec extends WordSpec {

  "The runtimeClassOf function" should {
    "throw an illegal argument exception" in {
      intercept[IllegalArgumentException] {
        runtimeClassOf
      }
    }
  }
}
