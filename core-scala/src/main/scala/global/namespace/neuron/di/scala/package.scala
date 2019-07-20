/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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
package global.namespace.neuron.di

import _root_.java.lang.reflect.Method
import _root_.java.util.function.{Function => jFunction}

import global.namespace.neuron.di.java.{DependencyProvider, DependencyResolver, CachingStrategy => jCachingStrategy}

import _root_.scala.language.experimental.macros
import _root_.scala.reflect.macros.blackbox
import _root_.scala.reflect.{ClassTag, classTag}

package object scala {

  type CachingStrategy = jCachingStrategy
  type MethodBinding = PartialFunction[Method, () => _]
  type SynapseBinding = Method => () => _

  object CachingStrategy {

    val DISABLED: CachingStrategy = jCachingStrategy.DISABLED
    val NOT_THREAD_SAFE: CachingStrategy = jCachingStrategy.NOT_THREAD_SAFE
    val THREAD_SAFE: CachingStrategy = jCachingStrategy.THREAD_SAFE
    val THREAD_LOCAL: CachingStrategy = jCachingStrategy.THREAD_LOCAL
  }

  /** Breeds a neuron of the given type, wiring each synapse to a value with the same name and an assignment-compatible
    * return type or to a function with the same name and an assignment-compatible return type which accepts the given
    * type or any supertype as its sole parameter.
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
    *   val foo = wire[Foo[String]]
    *   println(foo.c)
    * }
    * }}}
    *
    * When run, `Main` will print `Hello, World!` to standard output.
    * When calling `wire[Foo[String]]`, the type parameter of `Foo` is set to `String`, so the synapses `a`,
    * `b` and `c` each return a `String`.
    * The synapses are bound to their dependencies as follows:
    * + The synapse `a` is bound to the value `a` of type `String`.
    * + The synapse `b` is bound to the function definition `b` which accepts a parameter of the type `Foo[String]`.
    * + The synapse `c` is bound to the function value `c` which again accepts a parameter of the type `Foo[String]`.
    *
    * Finally, when calling `foo.c`, the function value `c` will call the function definition `b` which in turn will
    * call the value `a` to compute `"Hello, World!"`.
    *
    * @since Neuron DI 5.0 (renamed from `neuron`, which was introduced in Neuron DI 4.2)
    */
  def wire[A <: AnyRef]: A = macro Neuron.wire[A]

  def runtimeClassOf[A](implicit tag: ClassTag[A]): Class[A] = {
    require(tag != classTag[Nothing], "Missing type parameter.")
    tag.runtimeClass.asInstanceOf[Class[A]]
  }

  private[scala] implicit class DependencyProviderAdapter[A](supplier: () => A) extends DependencyProvider[A] {

    def get(): A = supplier()
  }

  private[scala] implicit class DependencyResolverAdapter[A, B](function: A => B) extends DependencyResolver[A, B] {

    def apply(a: A): B = function(a)
  }

  private[scala] implicit class SynapseBindingAdapter(binding: SynapseBinding) extends jFunction[Method, DependencyProvider[_]] {

    def apply(method: Method): DependencyProvider[_] = binding(method)
  }

  private[scala] def isCachingAnnotation(c: blackbox.Context)(annotation: c.Tree): Boolean = {
    isAnnotationType(c)(annotation)(JavaCachingAnnotationName)
  }

  private[scala] def isNeuronAnnotation(c: blackbox.Context)(annotation: c.Tree): Boolean = {
    isAnnotationType(c)(annotation)(JavaNeuronAnnotationName)
  }

  private[this] def isAnnotationType(c: blackbox.Context)(where: c.Tree)(what: String): Boolean = {

    import c.universe._

    val visited = collection.mutable.Set.empty[Symbol]

    def _isAnnotation(annotation: Tree): Boolean = {
      val tpe = c.typecheck(tree = annotation, mode = c.TYPEmode, silent = true).tpe
      what == tpe.toString || _hasAnnotation(tpe.typeSymbol)
    }

    def _hasAnnotation(symbol: Symbol): Boolean = {
      (visited add symbol) && (symbol.annotations exists (a => _isAnnotation(a.tree)))
    }

    _isAnnotation(where)
  }

  private[this] val JavaCachingAnnotationName = classOf[global.namespace.neuron.di.java.Caching].getName
  private[this] val JavaNeuronAnnotationName = classOf[global.namespace.neuron.di.java.Neuron].getName
}
