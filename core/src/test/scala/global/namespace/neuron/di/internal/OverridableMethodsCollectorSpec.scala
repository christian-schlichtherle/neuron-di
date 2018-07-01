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

import global.namespace.neuron.di.java.sample.Baz
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

class OverridableMethodsCollectorSpec extends WordSpec {

  "An overridable methods collector" should {
    "collect overridable methods" in {
      val methods = new OverridableMethodsCollector(classOf[Baz].getPackage)
        .collect(classOf[Baz])
        .asScala
        .map(_.toString)
        .toSet
      methods shouldBe Set(
        "public abstract java.lang.String global.namespace.neuron.di.java.sample.Baz.baz()",
        "protected java.lang.String global.namespace.neuron.di.java.sample.Baz.fooBar()",
        "java.lang.String global.namespace.neuron.di.java.sample.Baz.fooBarBaz()",
        "protected abstract java.lang.String global.namespace.neuron.di.java.sample.Baz.bar()",
        "protected abstract java.lang.String global.namespace.neuron.di.java.sample.bar.Bar.foo()",
        "protected void java.lang.Object.finalize() throws java.lang.Throwable",
        "public boolean java.lang.Object.equals(java.lang.Object)",
        "public java.lang.String java.lang.Object.toString()",
        "public native int java.lang.Object.hashCode()",
        "protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException"
      )
    }
  }
}
