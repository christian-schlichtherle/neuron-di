package global.tranquillity.neuron.di.core.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction, Supplier => jSupplier}

import global.tranquillity.neuron.di.core.Incubator.{Stubbing => jStubbing}
import global.tranquillity.neuron.di.core.scala.Incubator._
import global.tranquillity.neuron.di.core.{Incubator => jIncubator}

import scala.reflect.ClassTag

trait Incubator {

  final case class stub[A](implicit ct: ClassTag[A]) { stub =>

    private var stubbing: jStubbing[A] = jIncubator stub runtimeClassOf(ct)

    case class bind[B](methodReference: A => B) {

      private val methodStubbing = stubbing bind methodReference

      def to(value: B): stub.type = {
        stubbing = methodStubbing to value
        stub
      }

      def to[B2 <: B](replacement: A => B2): stub.type = {
        stubbing = methodStubbing to replacement
        stub
      }
    }

    def breed: A = stubbing.breed
  }

  final def breed[A](implicit ct: ClassTag[A]): A =
    jIncubator.breed(runtimeClassOf(ct))

  final def breed[A : ClassTag, B](binder: Method => () => B): A = {
    jIncubator.breed(runtimeClassOf[A],
      (method: Method) => binder(method): jSupplier[B]
    )
  }
}

object Incubator extends Incubator {

  implicit class FunctionAdapter[A, B](fun: A => B) extends jFunction[A, B] {

    def apply(a: A): B = fun(a)
  }

  implicit class SupplierAdapter[A](supplier: () => A) extends jSupplier[A] {

    def get(): A = supplier()
  }
}
