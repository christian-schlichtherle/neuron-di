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
package global.namespace.neuron.di.guice

import _root_.java.lang.annotation.Annotation

import com.google.inject.binder._
import com.google.inject.name.Names
import com.google.inject._
import global.namespace.neuron.di.guice.java.{BinderLike => jBinderLike, NeuronModule => jNeuronModule}
import global.namespace.neuron.di.scala.internal._

import reflect.ClassTag

package object scala {

  abstract class NeuronModule
    extends jNeuronModule
      with BinderLike {

    // As of Scala 2.11.8, `override implicit def binder: ...` doesn't work.
    @inline
    protected[this] implicit def $dontCallThisBinderExplicitly: Binder = binder
  }

  implicit class InjectorOps(val injector: Injector)
    extends InjectorLike

  trait InjectorLike {

    val injector: Injector

    def getInstanceOf[A : ClassTag]: A = injector getInstance runtimeClassOf[A]
  }

  implicit class BinderOps(val binder: Binder)
    extends BinderLike

  trait BinderLike extends jBinderLike {

    def bindNeuronClass[A >: Null : ClassTag]: ScopedBindingBuilder =
      bindNeuron(runtimeClassOf[A])

    def bindClass[A >: Null : ClassTag]: AnnotatedBindingBuilder[A] =
      binder bind runtimeClassOf[A]

    def getProviderClass[A >: Null : ClassTag]: Provider[A] =
      binder getProvider runtimeClassOf[A]
  }

  implicit class AnnotatedConstantBindingBuilderOps(val builder: AnnotatedConstantBindingBuilder)
    extends AnnotatedConstantBindingBuilderLike

  trait AnnotatedConstantBindingBuilderLike {

    def builder: AnnotatedConstantBindingBuilder

    def named(name: String): ConstantBindingBuilder =
      builder annotatedWith (Names named name)

    def annotatedWithClass[A <: Annotation : ClassTag]: ConstantBindingBuilder =
      builder annotatedWith runtimeClassOf[A]
  }

  implicit class AnnotatedBindingBuilderOps[A >: Null](val builder: AnnotatedBindingBuilder[A])
    extends AnnotatedBindingBuilderLike[A]

  trait AnnotatedBindingBuilderLike[A >: Null] {

    def builder: AnnotatedBindingBuilder[A]

    def named(name: String): LinkedBindingBuilder[A] =
      builder annotatedWith (Names named name)

    def annotatedWithClass[B <: Annotation : ClassTag]: LinkedBindingBuilder[A] =
      builder annotatedWith runtimeClassOf[B]
  }

  implicit class LinkedBindingBuilderOps[A >: Null](val builder: LinkedBindingBuilder[A])
    extends LinkedBindingBuilderLike[A]

  trait LinkedBindingBuilderLike[A >: Null] {

    def builder: LinkedBindingBuilder[A]

    /** @since Neuron DI 3.2 */
    def toNeuronClass[B <: A : ClassTag](implicit binder: Binder): ScopedBindingBuilder =
      toNeuron(TypeLiteral get runtimeClassOf[B])

    /** @since Neuron DI 3.2 */
    def toNeuron[B <: A](typeLiteral: TypeLiteral[B])(implicit binder: Binder): ScopedBindingBuilder =
      toNeuron(Key get typeLiteral)

    /** @since Neuron DI 3.2 */
    def toNeuron[B <: A](key: Key[B])(implicit binder: Binder): ScopedBindingBuilder =
      builder toProvider (binder neuronProvider key.getTypeLiteral)

    def toClass[B <: A : ClassTag]: ScopedBindingBuilder =
      builder to runtimeClassOf[B]

    def toProviderClass[B <: Provider[_ <: A] : ClassTag]: ScopedBindingBuilder =
      builder toProvider runtimeClassOf[B]
  }

  implicit class ScopedBindingBuilderOps(val builder: ScopedBindingBuilder)
    extends ScopedBindingBuilderLike

  trait ScopedBindingBuilderLike {

    def builder: ScopedBindingBuilder

    def inScope[A <: Annotation](implicit ct: ClassTag[A]) {
      builder in runtimeClassOf(ct)
    }
  }
}
