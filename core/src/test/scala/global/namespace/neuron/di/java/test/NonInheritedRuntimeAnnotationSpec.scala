package global.namespace.neuron.di.java.test

import global.namespace.neuron.di.java._
import global.namespace.neuron.di.java.sample.{NonInheritedRuntimeAnnotation, NonInheritedRuntimeAnnotationNeuron}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class NonInheritedRuntimeAnnotationSpec extends AnyWordSpec {

  "A non-inherited runtime annotation on a @Neuron class" should {
    "be copied to the proxy class" in {
      val annotationClass = classOf[NonInheritedRuntimeAnnotation]
      val neuronClass = classOf[NonInheritedRuntimeAnnotationNeuron]

      val neuron = Incubator breed neuronClass
      val proxyClass = neuron.getClass
      proxyClass should not be neuronClass
      val superClass: Class[_] = proxyClass.getSuperclass
      superClass shouldBe neuronClass

      Option(proxyClass getDeclaredAnnotation annotationClass) shouldBe defined
    }
  }
}
