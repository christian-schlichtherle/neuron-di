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
import global.namespace.neuron.di.guice.scala._
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar.mock

class BinderLikeSpec extends WordSpec {

  "A BinderLike" when {
    "using a mock Binder" should {
      val binder = mock[Binder]
      when(binder skipSources any[Class[_]]) thenReturn binder

      "bind a named constant" in {
        val builder1 = mock[AnnotatedConstantBindingBuilder]
        val builder2 = mock[ConstantBindingBuilder]

        when(binder.bindConstant) thenReturn builder1
        when(builder1 annotatedWith named("foo")) thenReturn builder2

        binder bindConstantNamed "foo" should be theSameInstanceAs builder2
      }

      "bind a neuron" in {
        val builder1 = mock[AnnotatedBindingBuilder[BinderLike]]
        val builder2 = mock[ScopedBindingBuilder]
        val injectorProvider = mock[Provider[Injector]]
        val injector = mock[Injector]

        when(binder bind (Key get classOf[BinderLike])) thenReturn builder1
        when(builder1 toProvider any[Provider[BinderLike]]) thenReturn builder2
        when(binder.getProviderClass[Injector]) thenReturn injectorProvider
        when(injectorProvider.get) thenReturn injector

        binder.bindNeuronClass[BinderLike] should be theSameInstanceAs builder2

        verify(binder).getProviderClass[Injector]

        val neuronProviderCaptor = ArgumentCaptor forClass classOf[Provider[BinderLike]]
        verify(builder1) toProvider neuronProviderCaptor.capture
        val neuronProvider = neuronProviderCaptor.getValue.asInstanceOf[NeuronProvider[BinderLike]]
        neuronProvider.injector should be theSameInstanceAs injector
        neuronProvider.typeLiteral should be (TypeLiteral get classOf[BinderLike])

        verify(injectorProvider).get
        verifyNoMoreInteractions(injector)
      }
    }
  }
}
