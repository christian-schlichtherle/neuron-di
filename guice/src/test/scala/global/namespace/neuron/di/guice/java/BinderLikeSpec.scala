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

class BinderLikeSpec extends WordSpec {

  "A BinderLike" should afterWord("bind a") {
    val binderLike = new BinderLike {

      val binder: Binder = mock[Binder]

      when(binder skipSources any[Class[_]]) thenReturn binder
    }
    import binderLike.binder

    "named constant" in {
      val builder1 = mock[AnnotatedConstantBindingBuilder]
      val builder2 = mock[ConstantBindingBuilder]

      when(binder.bindConstant) thenReturn builder1
      when(builder1 annotatedWith named("foo")) thenReturn builder2

      binderLike bindConstantNamed "foo" should be theSameInstanceAs builder2
    }

    "neuron interface" in {
      val typeLiteral = TypeLiteral get classOf[TestInterface]

      val builder1 = mock[AnnotatedBindingBuilder[TestInterface]]
      val builder2 = mock[ScopedBindingBuilder]
      val injectorProvider = mock[Provider[Injector]]
      val injector = mock[Injector]

      when(binder bind (Key get classOf[TestInterface])) thenReturn builder1
      when(builder1 toProvider any[Provider[TestInterface]]) thenReturn builder2
      when(binder getProvider classOf[Injector]) thenReturn injectorProvider
      when(injectorProvider.get) thenReturn injector

      binderLike bindNeuron classOf[TestInterface] should be theSameInstanceAs builder2

      val neuronProviderCaptor = ArgumentCaptor forClass classOf[NeuronProvider[TestInterface]]
      verify(builder1) toProvider neuronProviderCaptor.capture
      val neuronProvider = neuronProviderCaptor.getValue
      neuronProvider.injector should be theSameInstanceAs injector
      neuronProvider.membersInjector should not be null
      neuronProvider.typeLiteral shouldBe typeLiteral
    }

    "neuron class" in {
      val typeLiteral = TypeLiteral get classOf[TestClass]

      val builder1 = mock[AnnotatedBindingBuilder[TestClass]]
      val builder2 = mock[ScopedBindingBuilder]
      val injectorProvider = mock[Provider[Injector]]
      val injector = mock[Injector]
      val membersInjector = mock[MembersInjector[TestClass]]

      when(binder bind (Key get classOf[TestClass])) thenReturn builder1
      when(builder1 toProvider any[Provider[TestClass]]) thenReturn builder2
      when(binder getProvider classOf[Injector]) thenReturn injectorProvider
      when(injectorProvider.get) thenReturn injector
      when(binder getMembersInjector typeLiteral) thenReturn membersInjector

      binderLike bindNeuron classOf[TestClass] should be theSameInstanceAs builder2

      val neuronProviderCaptor = ArgumentCaptor forClass classOf[NeuronProvider[TestClass]]
      verify(builder1) toProvider neuronProviderCaptor.capture
      val neuronProvider = neuronProviderCaptor.getValue
      neuronProvider.injector should be theSameInstanceAs injector
      neuronProvider.membersInjector should be theSameInstanceAs membersInjector
      neuronProvider.typeLiteral shouldBe typeLiteral
    }
  }
}

private object BinderLikeSpec {

  @Neuron
  trait TestInterface

  @Neuron
  abstract class TestClass
}
