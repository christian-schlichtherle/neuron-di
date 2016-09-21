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

import com.google.inject.Module
import global.namespace.neuron.di.guice.sample.{Bar, BarImpl, FooImpl}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class FooBarModuleSpec extends WordSpec with ModuleSpec {

  override def module: Module = new FooBarModule

  "Make foos and bars" in {
    val bar1 = getInstanceOf[Bar]
    val bar2 = getInstanceOf[Bar]
    bar1 should not be theSameInstanceAs(bar2)
    bar1 shouldBe a[BarImpl]
    bar2 shouldBe a[BarImpl]
    bar1.foo shouldBe a[FooImpl]
    bar1.foo should be theSameInstanceAs bar2.foo
    bar1.foo.i shouldBe 1
  }
}
