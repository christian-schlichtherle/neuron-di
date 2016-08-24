package global.tranquillity.neuron.di.core.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction}

import global.tranquillity.neuron.di.core.{Incubator => jIncubator}

import scala.reflect.ClassTag

trait Incubator {

  final def breed[A](implicit ct: ClassTag[A]): A =
    jIncubator.breed(runtimeClassOf(ct))

  final def breed[A : ClassTag](dependency: Method => AnyRef): A = {
    jIncubator.breed(runtimeClassOf[A], new jFunction[Method, AnyRef] {

      def apply(method: Method): AnyRef = dependency(method)
    })
  }
}

object Incubator extends Incubator
