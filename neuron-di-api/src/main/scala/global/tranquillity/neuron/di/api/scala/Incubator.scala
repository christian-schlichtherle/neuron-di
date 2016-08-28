package global.tranquillity.neuron.di.api.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction, Supplier => jSupplier}
import global.tranquillity.neuron.di.api.{Incubator => jIncubator}
import scala.reflect._

object Incubator {

  final case class stub[A](implicit classTag: ClassTag[A]) { self =>

    private var jstub = jIncubator stub runtimeClassOf[A]

    case class bind[B](methodReference: A => B) {

      private val jbind = jstub bind methodReference

      def to(value: B): self.type = {
        jstub = jbind to value
        self
      }

      def to[B2 <: B](replacement: A => B2): self.type = {
        jstub = jbind to replacement
        self
      }
    }

    def breed: A = jstub.breed
  }

  final def breed[A : ClassTag]: A =
    jIncubator breed runtimeClassOf[A]

  final def breed[A : ClassTag](binder: Method => () => _): A = {
    jIncubator.breed(runtimeClassOf[A],
      (method: Method) => binder(method): jSupplier[_])
  }

  private implicit class FunctionAdapter[A, B](fun: A => B) extends jFunction[A, B] {

    def apply(a: A): B = fun(a)
  }

  private implicit class SupplierAdapter[A](supplier: () => A) extends jSupplier[A] {

    def get(): A = supplier()
  }
}
