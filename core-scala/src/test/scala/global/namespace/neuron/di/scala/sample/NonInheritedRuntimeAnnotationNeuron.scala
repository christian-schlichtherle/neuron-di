package global.namespace.neuron.di.scala.sample

import global.namespace.neuron.di.scala.Neuron

@Neuron
@NonInheritedRuntimeAnnotation
trait NonInheritedRuntimeAnnotationNeuron {

  var i = 1 // trigger the use of an abstract shim class
}
