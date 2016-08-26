package global.tranquillity.neuron.di.core.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction, Supplier => jSupplier}

import global.tranquillity.neuron.di.core.{Incubator => jIncubator}

import scala.reflect.ClassTag

trait Incubator {

  final def breed[A](implicit ct: ClassTag[A]): A =
    jIncubator.breed(runtimeClassOf(ct))

  final def breed[A : ClassTag, B](binder: Method => () => B): A = {
    jIncubator.breed(runtimeClassOf[A], new jFunction[Method, jSupplier[_]] {

      def apply(method: Method): jSupplier[B] = {
        val binding = binder(method)
        new jSupplier[B] {

          def get(): B = binding()
        }
      }
    })
  }
}

object Incubator extends Incubator
