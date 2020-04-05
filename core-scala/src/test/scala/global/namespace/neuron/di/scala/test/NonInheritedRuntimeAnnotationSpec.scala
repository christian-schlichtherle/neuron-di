package global.namespace.neuron.di.scala.test

import global.namespace.neuron.di.scala._
import global.namespace.neuron.di.scala.sample.{NonInheritedRuntimeAnnotation, NonInheritedRuntimeAnnotationNeuron}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class NonInheritedRuntimeAnnotationSpec extends AnyWordSpec {

  "A non-inherited runtime annotation on a @Neuron class" should {
    "be copied to the proxy class" in {
      val annotationClass = classOf[NonInheritedRuntimeAnnotation]
      val neuronClass = classOf[NonInheritedRuntimeAnnotationNeuron]

      val neuron = Incubator.breed[NonInheritedRuntimeAnnotationNeuron]
      val proxyClass = neuron.getClass
      proxyClass should not be neuronClass
      val shimClass: Class[_] = proxyClass.getSuperclass
      shimClass should not be neuronClass
      val interfaceClasses = shimClass.getInterfaces
      interfaceClasses should have size 1
      interfaceClasses(0) shouldBe neuronClass

      Option(proxyClass getDeclaredAnnotation annotationClass) shouldBe defined
    }
  }
}
