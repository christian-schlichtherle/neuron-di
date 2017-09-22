/*
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
package global.namespace.neuron.di.scala

import java.lang.reflect.Method
import java.util.function.{Function => jFunction}

import global.namespace.neuron.di.scala.internal.runtimeClassOf
import global.namespace.neuron.di.java.{DependencyProvider, DependencyResolver, Incubator => jIncubator}

import scala.reflect._

object Incubator {

  def breed[A >: Null : ClassTag]: A = jIncubator breed runtimeClassOf[A]

  def breed[A >: Null : ClassTag](binding: Method => () => _): A =
    jIncubator.breed(runtimeClassOf[A], (method: Method) => binding(method): DependencyProvider[_])

  case class wire[A >: Null : ClassTag]() { self =>

    private var jwire = jIncubator wire runtimeClassOf[A]

    def partial(value: Boolean): self.type = {
      jwire = jwire partial value
      self
    }

    case class bind[B](methodReference: A => B) {

      private val jbind = jwire bind methodReference

      def to(value: => B): self.type = {
        jwire = jbind to value _
        self
      }

      def to[C <: B](function: A => C): self.type = {
        jwire = jbind to function
        self
      }
    }

    def using(delegate: AnyRef): A = jwire using delegate

    def breed: A = jwire.breed
  }

  private implicit class FunctionAdapter[A, B](fun: A => B) extends jFunction[A, B] {

    def apply(a: A): B = fun(a)
  }

  private implicit class DependencyResolverAdapter[A, B](fun: A => B) extends DependencyResolver[A, B] {

    def apply(a: A): B = fun(a)
  }

  private implicit class DependencyProviderAdapter[A](supplier: () => A) extends DependencyProvider[A] {

    def get(): A = supplier()
  }
}
