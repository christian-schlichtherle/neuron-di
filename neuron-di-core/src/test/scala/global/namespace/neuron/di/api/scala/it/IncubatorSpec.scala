package global.namespace.neuron.di.api.scala.it

import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.sample.{Counter, HasDependency, Metric}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class IncubatorSpec extends WordSpec {

  "The incubator" should {
    "breed an empty string" in {
      Incubator.breed[String] shouldBe 'empty
    }

    "throw an exception when trying to breed an instance of a non-neuron interface" in {
      intercept[InstantiationError] {
        Incubator.breed[HasDependency[AnyRef]]
      }.getCause shouldBe an[InstantiationException]
    }

    "stub a metric neuron while complying to the configured caching strategies of its synapse methods" in {
      val a = new Counter
      val b = new Counter

      val metric = Incubator
        .stub[Metric]
        .bind(_.a).to(_ => a.inc)
        .bind(_.b).to(_ => b.inc)
        .breed

      metric.a shouldBe theSameInstanceAs(a)
      metric.b shouldBe theSameInstanceAs(b)

      metric.a shouldBe theSameInstanceAs(a)
      metric.b shouldBe theSameInstanceAs(b)

      a.count shouldBe 1
      b.count shouldBe 2
    }
  }
}
