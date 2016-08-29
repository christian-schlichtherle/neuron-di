package global.namespace.neuron.di.sample.scala

import javax.inject.Singleton

import com.google.inject.Guice
import global.namespace.neuron.di.guice.scala._
import global.namespace.neuron.di.sample.test.{Formatter, Greeting, RealFormatter}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class NeuronDiGuiceSpec extends WordSpec {

  "The Neuron DI Guice API" should {
    "make a greeting" in {
      val module = new NeuronModule {

        def configure() {
          bindNeuronClass[Greeting].inScope[Singleton]
          bindClass[Formatter].toClass[RealFormatter]
          bindConstantNamed("format").to("Hello %s!")
        }
      }
      val injector = Guice createInjector module
      val greeting = injector.getInstanceOf[Greeting]
      injector.getInstanceOf[Greeting] should be theSameInstanceAs greeting
      greeting.formatter shouldBe a[RealFormatter]
      greeting.formatter should be theSameInstanceAs greeting.formatter
      greeting.message shouldBe "Hello Christian!"
    }
  }
}
