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
package global.namespace.neuron.di.java

import global.namespace.neuron.di.java.sample.{A, HasPrivateMembers}
import org.scalatest.Matchers._
import org.scalatest.{Ignore, WordSpec}

import scala.reflect.ClassTag

class ReflectionSpec extends WordSpec {

  "Reflection.find(...)(...)" when {
    "looking up members in a subclass of `A`" should {
      val a = new A { }

      "find `a`" in {
        find[A]("a", a) should be theSameInstanceAs a
      }
    }

    "looking up members in a subclass of `HasPrivateMembers`" should {
      val hasPrivateMembers = new HasPrivateMembers { }

      "find `method`" in {
        find[Int]("method", hasPrivateMembers) shouldBe 1
      }

      "find `staticMethod`" in {
        find[Long]("staticMethod", hasPrivateMembers) shouldBe 2L
      }

      "find `field`" in {
        find[Boolean]("field", hasPrivateMembers) shouldBe true
      }

      "find `staticField`" in {
        find[Char]("staticField", hasPrivateMembers) shouldBe '?'
      }
    }
  }

  private def find[A : ClassTag](what: String, where: AnyRef): A = {
    Reflection.find(where.getClass, what).get()(where).invokeExact().asInstanceOf[A]
  }
}
