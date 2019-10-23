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
package global.namespace.neuron.di.internal

import global.namespace.neuron.di.internal.Reflection.overridableMethods
import global.namespace.neuron.di.java.sample.{Baz, StringFunction}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

class ReflectionSpec extends WordSpec {

  "An overridable methods collector" should {
    "collect overridable methods" in {
      val methods = setOfOverridableMethodsOf[Baz]
      methods shouldBe Set(
        "java.lang.String global.namespace.neuron.di.java.sample.Baz.fooBarBaz()",
        "protected abstract java.lang.String global.namespace.neuron.di.java.sample.Baz.bar()",
        "protected abstract java.lang.String global.namespace.neuron.di.java.sample.bar.Bar.foo()",
        "protected java.lang.String global.namespace.neuron.di.java.sample.Baz.fooBar()",
        "protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException",
        "protected void java.lang.Object.finalize() throws java.lang.Throwable",
        "public abstract java.lang.String global.namespace.neuron.di.java.sample.Baz.baz()",
        "public boolean java.lang.Object.equals(java.lang.Object)",
        "public java.lang.String java.lang.Object.toString()",
        "public native int java.lang.Object.hashCode()",
      )
    }

    "dito" in {
      val methods = setOfOverridableMethodsOf[StringFunction]
      methods shouldBe Set(
        "protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException",
        "protected void java.lang.Object.finalize() throws java.lang.Throwable",
        "public boolean java.lang.Object.equals(java.lang.Object)",
        "public default java.util.function.Function java.util.function.Function.andThen(java.util.function.Function)",
        "public default java.util.function.Function java.util.function.Function.compose(java.util.function.Function)",
        "public java.lang.String global.namespace.neuron.di.java.sample.StringFunction.apply(java.lang.String)",
        "public java.lang.String java.lang.Object.toString()",
        "public native int java.lang.Object.hashCode()",
      )
    }
  }

  private def setOfOverridableMethodsOf[A : ClassTag]: Set[String] = {
    overridableMethods(implicitly[ClassTag[A]].runtimeClass).asScala.map(_.toString).toSet
  }
}
