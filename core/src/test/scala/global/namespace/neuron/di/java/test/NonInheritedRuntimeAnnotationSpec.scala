package global.namespace.neuron.di.java.test

import global.namespace.neuron.di.java._
import global.namespace.neuron.di.java.sample.{NonInheritedRuntimeAnnotation, NonInheritedRuntimeAnnotationNeuron}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class NonInheritedRuntimeAnnotationSpec extends WordSpec {

  "A non-inherited runtime annotation on a @Neuron class" should {
    "be copied to the proxy class" in {
      val annotationClass = classOf[NonInheritedRuntimeAnnotation]
      val neuronClass = classOf[NonInheritedRuntimeAnnotationNeuron]

      val neuron = Incubator breed neuronClass
      val proxyClass = neuron.getClass
      proxyClass should not be neuronClass
      val superClass = proxyClass.getSuperclass
      superClass shouldBe neuronClass

      Option(proxyClass getDeclaredAnnotation annotationClass) shouldBe defined
    }
  }
}
