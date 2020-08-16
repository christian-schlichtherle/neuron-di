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
package global.namespace.neuron.di.scala

import java.util.function.Supplier

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class PackageObjectSpec extends AnyWordSpec {

  "The runtimeClassOf function" should {
    "return the runtime class of a string supplier" in {
      runtimeClassOf[Supplier[String]] shouldBe classOf[Supplier[_]]
    }

    "throw an illegal argument exception if no type parameter was provided" in {
      intercept[IllegalArgumentException] {
        runtimeClassOf
      }.getMessage shouldBe "requirement failed: Missing type parameter."
    }
  }
}
