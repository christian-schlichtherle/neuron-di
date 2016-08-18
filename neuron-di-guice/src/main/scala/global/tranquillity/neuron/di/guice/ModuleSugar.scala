package global.tranquillity.neuron.di.guice

import java.lang.annotation.Annotation

import com.google.inject.AbstractModule
import com.google.inject.binder._
import global.tranquillity.neuron.di.guice.ModuleSugar._

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

abstract class ModuleSugar extends AbstractModule {

  protected def bind_[A <: AnyRef : ClassTag]: AnnotatedBindingBuilder[A] =
    binder bind runtimeClassOf[A]

  implicit class WithAnnotatedConstantBindingBuilder(builder: AnnotatedConstantBindingBuilder) {

    def annotatedWith_[A <: Annotation : ClassTag]: ConstantBindingBuilder =
      builder annotatedWith runtimeClassOf[A]
  }

  implicit class WithAnnotatedBindingBuilder[A <: AnyRef](builder: AnnotatedBindingBuilder[A]) {

    def annotatedWith_[B <: Annotation : ClassTag]: LinkedBindingBuilder[A] =
      builder annotatedWith runtimeClassOf[B]
  }

  implicit class WithLinkedBindingBuilder[A <: AnyRef](builder: LinkedBindingBuilder[A]) {

    def to_[B <: A : ClassTag]: ScopedBindingBuilder =
      builder to runtimeClassOf[B]
  }

  implicit class WithScopedBindingBuilder(builder: ScopedBindingBuilder) {

    def in_[A <: Annotation : ClassTag] { builder in runtimeClassOf[A] }
  }
}

object ModuleSugar {

  private def runtimeClassOf[A](implicit ct: ClassTag[A]) = {
    require(ct != classTag[Nothing],
      s"Missing type parameter when using the `${classTag[ModuleSugar]}` DSL.")
    ct.runtimeClass.asInstanceOf[Class[A]]
  }
}
