package global.tranquillity.neuron.di.core.scala.it

import global.tranquillity.neuron.di.core.it.HasDependency
import global.tranquillity.neuron.di.core.scala.Incubator._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class IncubatorSpec extends WordSpec {

  "The incubator" should {
    "breed an empty string" in {
      breed[String] shouldBe 'empty
    }

    "throw an exception when trying to breed an instance of a non-neuron interface" in {
      intercept[InstantiationError] {
        breed[HasDependency[AnyRef]]
      }.getCause shouldBe a[NoSuchMethodException]
    }
  }
}
