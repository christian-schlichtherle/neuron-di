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

import java.lang.invoke.MethodHandles._

import global.namespace.neuron.di.java.Reflection.methodHandle
import global.namespace.neuron.di.java.ReflectionSpec._
import global.namespace.neuron.di.java.sample.{A, HasPrivateMembers}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.reflect.ClassTag

class ReflectionSpec extends WordSpec {

  "Reflection.methodHandle" when {
    "looking up members in an anonymous subclass of `A`" should {
      val a = new A {}

      "find `a`" in {
        assertThat("a", a) is a
      }
    }

    "looking up members in an anonymous subclass of `HasPrivateMembers`" should {
      val hasPrivateMembers = new HasPrivateMembers {}

      "find `method`" in {
        assertThat("method", hasPrivateMembers) is 1
      }

      "find `staticMethod`" in {
        assertThat("staticMethod", hasPrivateMembers) is 2L
      }

      "find `field`" in {
        assertThat("field", hasPrivateMembers) is true
      }

      "find `staticField`" in {
        assertThat("staticField", hasPrivateMembers) is '?'
      }
    }
  }
}

private object ReflectionSpec {

  case class assertThat[A: ClassTag](what: String, where: AnyRef) {

    def is(result: A): Unit = {
      methodHandle(what, where, publicLookup()).invokeExact().asInstanceOf[A] shouldBe result
      methodHandle(what, where, lookup()).invokeExact().asInstanceOf[A] shouldBe result
    }
  }

}
