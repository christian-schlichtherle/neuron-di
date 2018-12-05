package global.namespace.neuron.di.scala.test

import java.util.function.Supplier

import global.namespace.neuron.di.scala._
import global.namespace.neuron.di.scala.test.BuilderSpec._
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}

class BuilderSpec extends FeatureSpec with GivenWhenThen {

  feature("The `Builder` class can build any accessible, static, non-final class with an accessible constructor without parameters") {

    scenario("Building an instance of `Trait1`") {

      Builder
        .wire[Trait1[String]]
        .bind(_.get).to("Hello world!")
        .build
        .get shouldBe "Hello world!"
    }

    scenario("Building an instance of `Supplier`") {

      pending
      Builder
        .wire[Supplier[String]]
        .bind(_.get).to("Hello world!")
        .build
        .get shouldBe "Hello world!"
    }
  }
}

private object BuilderSpec {

  trait Trait1[A] {

    def get: A
  }
}
