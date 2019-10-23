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
package global.namespace.neuron.di.guice.java

import java.lang.reflect.Method

import com.google.inject._
import com.google.inject.name.Names.named
import global.namespace.neuron.di.guice.java.NeuronProviderSpec._
import global.namespace.neuron.di.guice.java.sample.{NeuronWithQualifiedSynapses, TestBindingAnnotation, TestQualifier}
import global.namespace.neuron.di.java.{DependencyProvider, Incubator}
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

class NeuronProviderSpec extends WordSpec {

  "A neuron provider" should {
    "use a given injector to create providers for keys with qualifying and binding annotations" in {
      val injector = mock[Injector]
      val provider = Incubator.breed(
        classOf[NeuronProvider[NeuronWithQualifiedSynapses]],
        (method: Method) => {
          val result = method.getName match {
            case "injector" => injector
            case "membersInjector" => noOpMembersInjector
            case "typeLiteral" => TypeLiteral get classOf[NeuronWithQualifiedSynapses]
          }
          () => result
        }: DependencyProvider[_]
      )

      val fooKey = Key.get(classOf[String], named("foo"))
      val barKey = Key.get(classOf[String], named("bar"))
      val boomKey = Key.get(classOf[String], classOf[TestQualifier])
      val bangKey = Key.get(classOf[String], classOf[TestBindingAnnotation])

      when(injector getProvider fooKey) thenReturn mock[Provider[String]]
      when(injector getProvider barKey) thenReturn mock[Provider[String]]
      when(injector getProvider boomKey) thenReturn mock[Provider[String]]
      when(injector getProvider bangKey) thenReturn mock[Provider[String]]

      provider.get shouldBe a[NeuronWithQualifiedSynapses]

      verify(injector) getProvider fooKey
      verify(injector) getProvider barKey
      verify(injector) getProvider boomKey
      verify(injector) getProvider bangKey
      verifyNoMoreInteractions(injector)
    }
  }
}

private object NeuronProviderSpec {

  object noOpMembersInjector extends MembersInjector[NeuronWithQualifiedSynapses] {

    def injectMembers(instance: NeuronWithQualifiedSynapses): Unit = {}
  }

}
