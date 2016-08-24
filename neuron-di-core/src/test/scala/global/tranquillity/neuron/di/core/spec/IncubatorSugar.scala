package global.tranquillity.neuron.di.core.spec

import global.tranquillity.neuron.di.core.Incubator

import scala.reflect.ClassTag

trait IncubatorSugar {

  def breed[A](implicit ct: ClassTag[A]): A =
    Incubator.breed(ct.runtimeClass).asInstanceOf[A]
}

object IncubatorSugar extends IncubatorSugar
