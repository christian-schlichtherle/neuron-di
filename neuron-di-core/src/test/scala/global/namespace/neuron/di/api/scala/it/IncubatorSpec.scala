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
package global.namespace.neuron.di.api.scala.it

import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.sample.{Counter, HasDependency, Metric}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class IncubatorSpec extends WordSpec {

  "The incubator" should {
    "breed an empty string" in {
      Incubator.breed[String] shouldBe 'empty
    }

    "throw an exception when trying to breed an instance of a non-neuron interface" in {
      intercept[InstantiationError] {
        Incubator.breed[HasDependency[AnyRef]]
      }.getCause shouldBe an[InstantiationException]
    }

    "stub a metric neuron while complying to the configured caching strategies of its synapse methods" in {
      val a = new Counter
      val b = new Counter

      val metric = Incubator
        .stub[Metric]
        .bind(_.a).to(_ => a.inc)
        .bind(_.b).to(_ => b.inc)
        .breed

      metric.a shouldBe theSameInstanceAs(a)
      metric.b shouldBe theSameInstanceAs(b)

      metric.a shouldBe theSameInstanceAs(a)
      metric.b shouldBe theSameInstanceAs(b)

      a.count shouldBe 1
      b.count shouldBe 2
    }
  }
}
