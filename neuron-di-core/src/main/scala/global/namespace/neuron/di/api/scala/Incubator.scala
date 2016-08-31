/**
 * Copyright © 2016 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package global.namespace.neuron.di.api.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction, Supplier => jSupplier}

import global.namespace.neuron.di.api.{Incubator => jIncubator}

import scala.reflect._

object Incubator {

  final case class stub[A](implicit classTag: ClassTag[A]) { self =>

    private var jstub = jIncubator stub runtimeClassOf[A]

    case class bind[B](methodReference: A => B) {

      private val jbind = jstub bind methodReference

      def to(replacement: => B): self.type = {
        jstub = jbind to replacement _
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

  final def breed[A : ClassTag](resolve: Method => () => _): A = {
    jIncubator.breed(runtimeClassOf[A],
      (method: Method) => resolve(method): jSupplier[_])
  }

  private implicit class FunctionAdapter[A, B](fun: A => B) extends jFunction[A, B] {

    def apply(a: A): B = fun(a)
  }

  private implicit class SupplierAdapter[A](supplier: () => A) extends jSupplier[A] {

    def get(): A = supplier()
  }
}
