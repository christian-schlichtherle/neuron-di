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
package global.namespace.neuron.di.java

import java.lang.invoke.MethodHandle
import java.util.Optional

import global.namespace.neuron.di.java.Reflection._
import global.namespace.neuron.di.java.sample.{A, HasPrivateMembers}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class ReflectionSpec extends WordSpec {

  implicit class WithOptionalMethodHandle(omh: Optional[MethodHandle]) {

    def as[A]: A = {
      omh
        .get
        .invokeWithArguments() // workaround for Scala 2.11.[0,4], otherwise it should be `invokeExact()`
        .asInstanceOf[A]
    }
  }

  "Reflection.find(...).in(...)" when {
    "looking up members in a subclass of `A`" should {
      val a = new A { }

      "find `a`" in {
        find("a").in(a).as[A] should be theSameInstanceAs a
      }
    }

    "looking up members in a subclass of `HasPrivateMembers`" should {
      val hasPrivateMembers = new HasPrivateMembers { }

      "find `method`" in {
        find("method").in(hasPrivateMembers).as[Int] shouldBe 1
      }

      "find `staticMethod`" in {
        find("staticMethod").in(hasPrivateMembers).as[Long] shouldBe 2L
      }

      "find `field`" in {
        find("field").in(hasPrivateMembers).as[Boolean] shouldBe true
      }

      "find `staticField`" in {
        find("staticField").in(hasPrivateMembers).as[Char] shouldBe '?'
      }
    }
  }
}
