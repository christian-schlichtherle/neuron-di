package global.tranquillity.neuron.di.guice

import java.lang.annotation.Annotation
import javax.inject.Provider

import com.google.inject.AbstractModule
import com.google.inject.binder._
import global.tranquillity.neuron.di.core.Organism
import global.tranquillity.neuron.di.guice.ModuleSugar._

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

abstract class ModuleSugar extends AbstractModule {

  private lazy val organism = Organism.breed

  protected def bindNeuron[A <: AnyRef](implicit ct : ClassTag[A]) {
    bind_[A].toProvider(new Provider[A] {
      def get: A = organism make runtimeClassOf(ct)
    })
  }

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

    def toProvider_[B <: Provider[_ <: A] : ClassTag]: ScopedBindingBuilder =
      builder toProvider runtimeClassOf[B]
  }

  implicit class WithScopedBindingBuilder(builder: ScopedBindingBuilder) {

    def in_[A <: Annotation](implicit ct: ClassTag[A]) {
      builder in runtimeClassOf(ct)
    }
  }
}

object ModuleSugar {

  private def runtimeClassOf[A](implicit ct: ClassTag[A]) = {
    require(ct != classTag[Nothing],
      s"Missing type parameter when using the `${classTag[ModuleSugar]}` DSL.")
    ct.runtimeClass.asInstanceOf[Class[A]]
  }
}
