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

import com.google.inject.name.Names.named
import com.google.inject.{Injector, Key, Provider, TypeLiteral}
import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.guice.sample.{NeuronWithQualifiedSynapses, TestBindingAnnotation, TestQualifier}
import org.scalatest.WordSpec
import org.scalatest.Matchers._
import org.specs2.mock.Mockito

class NeuronProviderSpec extends WordSpec with Mockito {

  "A neuron provider" should {
    "use a given injector to create providers for keys with qualifying and binding annotations" in {
      val injector = mock[Injector]
      val provider = Incubator
        .stub[NeuronProvider[NeuronWithQualifiedSynapses]]
        .bind(_.injector).to(injector)
        .bind(_.typeLiteral).to(TypeLiteral get classOf[NeuronWithQualifiedSynapses])
        .breed
      val fooKey = Key.get(classOf[String], named("foo"))
      val barKey = Key.get(classOf[String], named("bar"))
      val boomKey = Key.get(classOf[String], classOf[TestQualifier])
      val bangKey = Key.get(classOf[String], classOf[TestBindingAnnotation])

      injector getProvider fooKey returns mock[Provider[String]]
      injector getProvider barKey returns mock[Provider[String]]
      injector getProvider boomKey returns mock[Provider[String]]
      injector getProvider bangKey returns mock[Provider[String]]

      provider.get shouldBe a[NeuronWithQualifiedSynapses]

      there was one(injector).getProvider(fooKey)
      there was one(injector).getProvider(barKey)
      there was one(injector).getProvider(boomKey)
      there was one(injector).getProvider(bangKey)
      there were noMoreCallsTo(injector)
    }
  }
}
