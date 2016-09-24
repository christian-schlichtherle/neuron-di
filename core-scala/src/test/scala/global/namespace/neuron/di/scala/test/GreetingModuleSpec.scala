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
package global.namespace.neuron.di.scala.test

import global.namespace.neuron.di.scala.sample.RealFormatter
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class GreetingModuleSpec extends WordSpec {

  "Make a greeting" in {
    val module = new GreetingModule
    val greeting = module.greeting
    module.greeting should be theSameInstanceAs greeting
    greeting.formatter shouldBe a[RealFormatter]
    greeting.formatter should not be theSameInstanceAs(greeting.formatter)
    greeting.message should be theSameInstanceAs greeting.message
    greeting.message shouldBe "Hello Christian!"
  }
}
