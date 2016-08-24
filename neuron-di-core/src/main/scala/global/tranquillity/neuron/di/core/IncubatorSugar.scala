package global.tranquillity.neuron.di.core

import java.lang.reflect.Method
import java.util.function.{Function => jFunction}

import scala.reflect.ClassTag

trait IncubatorSugar {

  def breed[A](implicit ct: ClassTag[A]): A =
    Incubator.breed(ct.runtimeClass).asInstanceOf[A]

  def breed[A](dependency: Method => AnyRef)(implicit ct: ClassTag[A]): A = {
    Incubator.breed(ct.runtimeClass, new jFunction[Method, AnyRef] {

      def apply(method: Method): AnyRef = dependency(method)
    }).asInstanceOf[A]
  }
}

object IncubatorSugar extends IncubatorSugar
