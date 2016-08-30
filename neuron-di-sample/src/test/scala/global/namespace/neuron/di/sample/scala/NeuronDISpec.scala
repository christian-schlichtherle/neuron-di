package global.namespace.neuron.di.sample.scala

import com.google.inject.Guice
import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.guice.scala._
import global.namespace.neuron.di.sample.test.{Formatter, Greeting, RealFormatter}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class NeuronDISpec extends WordSpec {

  def bePossibleUsingThe = afterWord("be possible using the")

  trait GreetingTest {

    val greeting: Greeting

    greeting.formatter shouldBe a[RealFormatter]
    greeting.formatter should be theSameInstanceAs greeting.formatter
    greeting.message shouldBe "Hello Christian!"
  }

  "Making a greeting" should bePossibleUsingThe {
    "Neuron DI Guice API" in new GreetingTest {
      lazy val greeting = injector.getInstanceOf[Greeting]
      def injector = Guice createInjector module
      def module = new NeuronModule {

        def configure() {
          bindNeuronClass[Greeting]
          bindClass[Formatter].toClass[RealFormatter]
          bindConstantNamed("format").to("Hello %s!")
        }
      }
    }

    "Neuron DI API" in new GreetingTest {
      lazy val greeting = Incubator
        .stub[Greeting]
        .bind(_.formatter).to(formatter)
        .breed
      def formatter = new RealFormatter("Hello %s!")
    }
  }
}