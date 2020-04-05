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
package global.namespace.neuron.di.guice.scala.test

import global.namespace.neuron.di.guice.scala.sample.{Formatter, Greeting}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

abstract class GreetingModuleSpec extends AnyWordSpec with ModuleSpec {

  "Make a greeting" in {
    val greeting = getInstanceOf[Greeting]
    greeting message "world" shouldBe "Hello world!"
    getInstanceOf[Greeting] should be theSameInstanceAs greeting
  }

  "Make a formatter" in {
    val formatter = getInstanceOf[Formatter]
    formatter format "world" shouldBe "Hello world!"
    getInstanceOf[Formatter] should not be theSameInstanceAs(formatter)
  }
}
