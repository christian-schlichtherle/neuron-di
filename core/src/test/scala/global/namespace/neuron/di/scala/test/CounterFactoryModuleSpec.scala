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

import global.namespace.neuron.di.scala.Incubator
import global.namespace.neuron.di.java.test.CounterFactoryModule
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class CounterFactoryModuleSpec extends WordSpec {

  "Make a counter factory" in {
    val module = Incubator.breed[CounterFactoryModule]
    val factory = module.counterFactory
    module.counterFactory should be theSameInstanceAs factory
    factory.counter should not be theSameInstanceAs(factory.counter)
  }
}