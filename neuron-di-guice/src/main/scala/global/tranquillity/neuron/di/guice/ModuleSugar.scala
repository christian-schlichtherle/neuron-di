package global.tranquillity.neuron.di.guice

import java.lang.annotation.Annotation
import javax.inject.Provider

import com.google.inject.binder._
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Binder}
import global.tranquillity.neuron.di.guice.ModuleSugar._

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

abstract class ModuleSugar extends AbstractModule with NeuronModule {

  override def binder(): Binder = super.binder()

  protected def bindNeuronClass[A <: AnyRef](implicit ct: ClassTag[A]) =
    bindNeuron(runtimeClassOf(ct))

  protected def bindClass[A <: AnyRef : ClassTag]: AnnotatedBindingBuilder[A] =
    binder bind runtimeClassOf[A]

  implicit class WithAnnotatedConstantBindingBuilder(builder: AnnotatedConstantBindingBuilder) {

    def named(name: String) = builder annotatedWith (Names named name)

    def annotatedWithClass[A <: Annotation : ClassTag]: ConstantBindingBuilder =
      builder annotatedWith runtimeClassOf[A]
  }

  implicit class WithAnnotatedBindingBuilder[A <: AnyRef](builder: AnnotatedBindingBuilder[A]) {

    def named(name: String) = builder annotatedWith (Names named name)

    def annotatedWithClass[B <: Annotation : ClassTag]: LinkedBindingBuilder[A] =
      builder annotatedWith runtimeClassOf[B]
  }

  implicit class WithLinkedBindingBuilder[A <: AnyRef](builder: LinkedBindingBuilder[A]) {

    def toClass[B <: A : ClassTag]: ScopedBindingBuilder =
      builder to runtimeClassOf[B]

    def toProviderClass[B <: Provider[_ <: A] : ClassTag]: ScopedBindingBuilder =
      builder toProvider runtimeClassOf[B]
  }

  implicit class WithScopedBindingBuilder(builder: ScopedBindingBuilder) {

    def inScope[A <: Annotation](implicit ct: ClassTag[A]) {
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
