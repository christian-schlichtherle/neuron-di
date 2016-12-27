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

import global.namespace.neuron.di.internal.scala.runtimeClassOf
import global.namespace.neuron.di.java.{DependencyProvider, DependencyResolver, Incubator => jIncubator}

import scala.language.experimental.macros
import scala.reflect._

object Incubator {

  /** Breeds a neuron of the given type, wiring each synapse to a value with the same name and return type or to a
    * function with the same name and return type which accepts the given type as its sole parameter.
    * Example:
    * {{{
    * @Neuron
    * trait Foo[A] {
    *   def a: A
    *   def b: A
    *   def c: A
    * }
    *
    * object Main extends App {
    *   val a = "World"
    *   def b(neuron: Foo[String]) = "Hello, " + neuron.a
    *   val c = (neuron: Foo[String]) => neuron.b + "!"
    *   val foo = Incubator.neuron[Foo[String]]
    *   println(foo.c)
    * }
    * }}}
    *
    * When run, `Main` will print `Hello, World!` to standard output.
    * When calling `Incubator.neuron[Foo[String]]`, the type parameter of `Foo` is set to `String`, so the synapses `a`,
    * `b` and `c` each return a `String`.
    * The synapses are bound to their dependencies as follows:
    * + The synapse `a` is bound to the value `a` of type `String`.
    * + The synapse `b` is bound to the function definition `b` which accepts a parameter of the type `Foo[String]`.
    * + The synapse `c` is bound to the function value `c` which again accepts a parameter of the type `Foo[String]`.
    *
    * Finally, when calling `foo.c`, the function value `c` will call the function definition `b` which in turn will
    * call the value `a` to compute `"Hello, World!"`.
    *
    * @since Neuron DI 4.2
    */
  def neuron[A <: AnyRef]: A = macro Neuron.wire[A]

  def breed[A <: AnyRef : ClassTag]: A = jIncubator breed runtimeClassOf[A]

  def breed[A <: AnyRef : ClassTag](binding: Method => () => _): A =
    jIncubator.breed(runtimeClassOf[A], (method: Method) => binding(method): DependencyProvider[_])

  case class stub[A <: AnyRef](implicit classTag: ClassTag[A]) {
    self =>

    private var jstub = jIncubator stub runtimeClassOf[A]

    def partial(value: Boolean): self.type = {
      jstub partial value
      self
    }

    case class bind[B](methodReference: A => B) {

      private val jbind = jstub bind methodReference

      def to(value: => B): self.type = {
        jstub = jbind to value _
        self
      }

      def to[C <: B](function: A => C): self.type = {
        jstub = jbind to function
        self
      }
    }

    def breed: A = jstub.breed
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
