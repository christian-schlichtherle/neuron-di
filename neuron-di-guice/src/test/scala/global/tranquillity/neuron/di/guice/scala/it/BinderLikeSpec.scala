package global.tranquillity.neuron.di.guice.scala.it

import javax.inject.Provider

import com.google.inject.binder.{AnnotatedBindingBuilder, AnnotatedConstantBindingBuilder, ConstantBindingBuilder, ScopedBindingBuilder}
import com.google.inject.name.Names.named
import com.google.inject.{Binder, Injector, Provider => gProvider}
import global.tranquillity.neuron.di.api.scala.Incubator
import global.tranquillity.neuron.di.guice.scala._
import global.tranquillity.neuron.di.guice.{BinderLike => jBinderLike}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.specs2.mock.Mockito

class BinderLikeSpec extends WordSpec with Mockito {

  "The binder-like Java API" should {

    val binder = mock[Binder]

    val binderLike = Incubator
      .stub[jBinderLike]
      .bind(_.binder).to(binder)
      .breed

    "bind named constants" in {
      val builder1 = mock[AnnotatedConstantBindingBuilder]
      val builder2 = mock[ConstantBindingBuilder]

      binder.bindConstant returns builder1
      builder1.annotatedWith(named("foo")) returns builder2

      binderLike.bindConstantNamed("foo") should be theSameInstanceAs builder2
    }

    "bind neurons" in {
      val builder1 = mock[AnnotatedBindingBuilder[jBinderLike]]
      val builder2 = mock[ScopedBindingBuilder]
      val injectorProvider = mock[gProvider[Injector]]

      binder.bindClass[jBinderLike] returns builder1
      builder1.toProvider(any[Provider[jBinderLike]]) returns builder2
      binder.getProvider(classOf[Injector]) returns injectorProvider

      binderLike.bindNeuron(classOf[jBinderLike]) should be theSameInstanceAs builder2

      there was one(binder).getProvider(classOf[Injector])

      val binderLikeProvider = ArgumentCaptor.forClass(classOf[Provider[jBinderLike]])
      verify(builder1).toProvider(binderLikeProvider.capture)
      binderLikeProvider.getValue.get shouldBe a[jBinderLike]

      there was one(injectorProvider).get
    }
  }
}
