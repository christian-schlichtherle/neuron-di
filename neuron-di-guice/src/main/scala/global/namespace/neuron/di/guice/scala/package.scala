package global.namespace.neuron.di.guice

import java.lang.annotation.Annotation
import javax.inject.Provider

import com.google.inject.binder._
import com.google.inject.name.Names
import com.google.inject.{Binder, Injector}
import global.namespace.neuron.di.api.scala._
import global.namespace.neuron.di.guice.{BinderLike => jBinderLike, NeuronModule => jNeuronModule}

import reflect.ClassTag

package object scala {

  abstract class NeuronModule
    extends jNeuronModule
      with BinderLike

  implicit class InjectorOps(val injector: Injector)
    extends InjectorLike

  trait InjectorLike {

    val injector: Injector

    def getInstanceOf[A](implicit ct: ClassTag[A]): A =
      injector.getInstance(runtimeClassOf(ct))
  }

  implicit class BinderOps(val binder: Binder)
    extends BinderLike

  trait BinderLike extends jBinderLike {

    def bindNeuronClass[A <: AnyRef](implicit ct: ClassTag[A]): ScopedBindingBuilder =
      bindNeuron(runtimeClassOf(ct))

    def bindClass[A <: AnyRef : ClassTag]: AnnotatedBindingBuilder[A] =
      binder bind runtimeClassOf[A]
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

  implicit class AnnotatedBindingBuilderOps[A <: AnyRef](val builder: AnnotatedBindingBuilder[A])
    extends AnnotatedBindingBuilderLike[A]

  trait AnnotatedBindingBuilderLike[A <: AnyRef] {

    def builder: AnnotatedBindingBuilder[A]

    def named(name: String): LinkedBindingBuilder[A] =
      builder annotatedWith (Names named name)

    def annotatedWithClass[B <: Annotation : ClassTag]: LinkedBindingBuilder[A] =
      builder annotatedWith runtimeClassOf[B]
  }

  implicit class LinkedBindingBuilderOps[A <: AnyRef](val builder: LinkedBindingBuilder[A])
    extends LinkedBindingBuilderLike[A]

  trait LinkedBindingBuilderLike[A <: AnyRef] {

    def builder: LinkedBindingBuilder[A]

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
