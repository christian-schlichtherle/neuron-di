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
package global.namespace.neuron.di.guice.java

import com.google.inject._
import com.google.inject.binder.{AnnotatedBindingBuilder, AnnotatedConstantBindingBuilder, ConstantBindingBuilder, ScopedBindingBuilder}
import com.google.inject.name.Names.named
import global.namespace.neuron.di.guice.java.BinderLikeSpec._
import global.namespace.neuron.di.java.Neuron
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar.mock

import scala.reflect._

class BinderLikeSpec extends WordSpec {

  val binderLike = new BinderLike {

    val binder: Binder = mock[Binder]

    when(binder skipSources any[Class[_]]) thenReturn binder
  }
  import binderLike.binder

  "A BinderLike" should afterWord("bind a") {

    "named constant" in {
      val builder1 = mock[AnnotatedConstantBindingBuilder]
      val builder2 = mock[ConstantBindingBuilder]

      when(binder.bindConstant) thenReturn builder1
      when(builder1 annotatedWith named("foo")) thenReturn builder2

      binderLike bindConstantNamed "foo" should be theSameInstanceAs builder2
    }

    "neuron interface using a class" in {
      testBindNeuronUsingClass[NeuronInterface]
    }

    "neuron interface using a type literal" in {
      testBindNeuronUsingTypeLiteral[NeuronInterface]
    }

    "neuron interface using a key" in {
      testBindNeuronUsingKey[NeuronInterface]
    }

    "neuron class using a class" in {
      testBindNeuronUsingClass[NeuronClass]
    }

    "neuron class using a type literal" in {
      testBindNeuronUsingTypeLiteral[NeuronClass]
    }

    "neuron clazz using a key" in {
      testBindNeuronUsingKey[NeuronClass]
    }

    "neuron classes and interfaces using classes" in {
      testBindNeuronsUsingClasses(classOf[NeuronInterface], classOf[NeuronClass])
    }

    "neuron classes and interfaces using type literals" in {
      testBindNeuronsUsingTypeLiterals(classOf[NeuronInterface], classOf[NeuronClass])
    }

    "neuron classes and interfaces using keys" in {
      testBindNeuronsUsingKeys(classOf[NeuronInterface], classOf[NeuronClass])
    }
  }

  private def testBindNeuronUsingClass[A](implicit classTag: ClassTag[A]) {
    testBindNeuron[A] { binderLike bindNeuron implicitly[Class[A]] }
  }

  private def testBindNeuronUsingTypeLiteral[A](implicit classTag: ClassTag[A]) {
    testBindNeuron[A] { binderLike bindNeuron implicitly[TypeLiteral[A]] }
  }

  private def testBindNeuronUsingKey[A](implicit classTag: ClassTag[A]) {
    testBindNeuron[A] { binderLike bindNeuron implicitly[Key[A]] }
  }

  private def testBindNeuron[A : ClassTag](bindingCall: => ScopedBindingBuilder) {
    val clazz = implicitly[Class[A]]
    val typeLiteral = implicitly[TypeLiteral[A]]
    val key = implicitly[Key[A]]

    val builder1 = mock[AnnotatedBindingBuilder[A]]
    val builder2 = mock[ScopedBindingBuilder]
    val injectorProvider = mock[Provider[Injector]]
    val injector = mock[Injector]
    val membersInjector = mock[MembersInjector[A]]

    when(binder bind clazz) thenReturn builder1
    when(binder bind typeLiteral) thenReturn builder1
    when(binder bind key) thenReturn builder1
    when(builder1 toProvider any[Provider[A]]) thenReturn builder2
    when(binder getProvider classOf[Injector]) thenReturn injectorProvider
    when(injectorProvider.get) thenReturn injector
    if (!clazz.isInterface) {
      when(binder getMembersInjector typeLiteral) thenReturn membersInjector
    }

    bindingCall should be theSameInstanceAs builder2

    val neuronProviderCaptor = ArgumentCaptor forClass classOf[NeuronProvider[A]]
    verify(builder1) toProvider neuronProviderCaptor.capture
    val neuronProvider = neuronProviderCaptor.getValue
    neuronProvider.injector should be theSameInstanceAs injector
    if (clazz.isInterface) {
      neuronProvider.membersInjector should not be null
    } else {
      neuronProvider.membersInjector should be theSameInstanceAs membersInjector
    }
    neuronProvider.typeLiteral shouldBe typeLiteral
  }

  private def testBindNeuronsUsingClasses(classes: Class[_ <: AnyRef]*) {
    testBindNeurons(classes: _*) { binderLike.bindNeurons(classes.head, classes.tail: _*) }
  }

  private def testBindNeuronsUsingTypeLiterals(classes: Class[_ <: AnyRef]*) {
    testBindNeurons(classes: _*) {
      binderLike.bindNeurons(TypeLiteral get classes.head, classes.tail.map(TypeLiteral get _): _*)
    }
  }

  private def testBindNeuronsUsingKeys(classes: Class[_ <: AnyRef]*) {
    testBindNeurons(classes: _*) {
      binderLike.bindNeurons(Key get classes.head, classes.tail.map(Key get _): _*)
    }
  }

  private def testBindNeurons(classes: Class[_ <: AnyRef]*)(bindingCall: => Unit) {
    val injectorProvider = mock[Provider[Injector]]
    val injector = mock[Injector]

    when(binder getProvider classOf[Injector]) thenReturn injectorProvider
    when(injectorProvider.get) thenReturn injector

    def stubbing[A <: AnyRef](clazz : Class[A]): (Class[A], AnnotatedBindingBuilder[A], MembersInjector[A]) = {
      val typeLiteral = TypeLiteral get clazz
      val key = Key get clazz

      val builder = mock[AnnotatedBindingBuilder[A]]
      val membersInjector = mock[MembersInjector[A]]

      when(binder bind clazz) thenReturn builder
      when(binder bind typeLiteral) thenReturn builder
      when(binder bind key) thenReturn builder
      if (!clazz.isInterface) {
        when(binder getMembersInjector typeLiteral) thenReturn membersInjector
      }
      (clazz, builder, membersInjector)
    }

    val stubbings = classes map (stubbing(_))

    bindingCall

    def bar[B <: AnyRef](x: (Class[B], AnnotatedBindingBuilder[B], MembersInjector[B])) {
      verification(x._1, x._2, x._3)
    }

    def verification[B <: AnyRef](clazz: Class[B], builder: AnnotatedBindingBuilder[B], membersInjector: MembersInjector[B]) {
      val typeLiteral = TypeLiteral get clazz
      val neuronProviderCaptor = ArgumentCaptor forClass classOf[NeuronProvider[B]]
      verify(builder) toProvider neuronProviderCaptor.capture
      val neuronProvider = neuronProviderCaptor.getValue
      neuronProvider.injector should be theSameInstanceAs injector
      if (clazz.isInterface) {
        neuronProvider.membersInjector should not be null
      } else {
        neuronProvider.membersInjector should be theSameInstanceAs membersInjector
      }
      neuronProvider.typeLiteral shouldBe typeLiteral
    }

    stubbings foreach (x => bar(x))
  }
}

private object BinderLikeSpec {

  implicit def keyOf[A](implicit classTag: ClassTag[A]): Key[A] = Key get implicitly[Class[A]]

  implicit def typeLiteralOf[A](implicit classTag: ClassTag[A]): TypeLiteral[A] = TypeLiteral get implicitly[Class[A]]

  implicit def runtimeClassOf[A](implicit classTag: ClassTag[A]): Class[A] =
    classTag.runtimeClass.asInstanceOf[Class[A]]

  @Neuron
  trait NeuronInterface

  @Neuron
  abstract class NeuronClass
}
