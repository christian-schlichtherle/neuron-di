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
package global.namespace.neuron.di.sample.it.scala

import com.google.inject.Guice
import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.guice.scala._
import global.namespace.neuron.di.sample.{Formatter, Greeting, RealFormatter}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class NeuronDISpec extends WordSpec {

  def bePossibleUsingThe = afterWord("be possible using the")

  "Making a greeting" should bePossibleUsingThe {
    "Neuron DI Guice API" in new GreetingTest {
      def module = new NeuronModule {

        def configure() {
          bindNeuronClass[Greeting]
          bindClass[Formatter].toClass[RealFormatter]
          bindConstantNamed("format").to("Hello %s!")
        }
      }
      def injector = Guice createInjector module
      lazy val greeting = injector.getInstanceOf[Greeting]
    }

    "Neuron DI API" in new GreetingTest {
      def formatter = new RealFormatter("Hello %s!")
      lazy val greeting = Incubator
        .stub[Greeting]
        .bind(_.formatter).to(formatter)
        .breed
    }
  }

  trait GreetingTest {

    val greeting: Greeting

    greeting.formatter shouldBe a[RealFormatter]
    greeting.formatter should be theSameInstanceAs greeting.formatter
    greeting.message shouldBe "Hello Christian!"
  }
}
