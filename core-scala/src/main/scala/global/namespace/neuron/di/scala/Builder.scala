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
import java.util.Optional

import global.namespace.neuron.di.internal.scala.runtimeClassOf
import global.namespace.neuron.di.java.{DependencyProvider, DependencyResolver, Builder => jBuilder, MethodBinding => jMethodBinding}

import scala.languageFeature.implicitConversions
import scala.reflect.ClassTag

/** @author Christian Schlichtherle */
object Builder {

  def build[A >: Null : ClassTag]: A = jBuilder build runtimeClassOf[A]

  def build[A >: Null : ClassTag](binding: MethodBinding): A = {
    jBuilder.build(runtimeClassOf[A], new jMethodBinding {

      def apply(method: Method): Optional[DependencyProvider[_]] = Optional.ofNullable(binding.applyOrElse(method, null))
    })
  }

  case class wire[A >: Null](implicit tag: ClassTag[A]) { self =>

    private var jwire = jBuilder wire runtimeClassOf[A]

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

    def build: A = jwire.build
  }

  private implicit class DependencyProviderAdapter[A](supplier: () => A) extends DependencyProvider[A] {

    def get(): A = supplier()
  }

  private implicit class DependencyResolverAdapter[A, B](fun: A => B) extends DependencyResolver[A, B] {

    def apply(a: A): B = fun(a)
  }
}
