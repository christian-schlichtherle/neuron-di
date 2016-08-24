package global.tranquillity.neuron.di.core.spec

import global.tranquillity.neuron.di.core.spec.IncubatorSugar._
import global.tranquillity.neuron.di.core.test.HasA
import org.scalatest.{FlatSpec, WordSpec}
import org.scalatest.Matchers._

class IncubatorSpec extends WordSpec {

  "The incubator" should {
    "breed an empty string" in {
      breed[String] shouldBe 'empty
    }

    "throw an exception when trying to breed an instance of a non-neuron interface" in {
      intercept[InstantiationError] {
        breed[HasA]
      }.getCause shouldBe a[NoSuchMethodException]
    }
  }
}
