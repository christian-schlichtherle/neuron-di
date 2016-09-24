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

import java.util.Date

package object sample {

  trait A {

    def a: A = this
  }

  trait B {

    def b: B = this
  }

  trait HasA {

    def a: A
  }

  trait HasB {

    def b: B
  }

  trait C {

    def c: C = this
  }

  trait HasC {

    def c: C
  }

  @Neuron
  trait SomeNeuronInterface extends HasA with HasB with HasC

  @Neuron
  abstract class SomeNeuronClass extends SomeNeuronInterface

  abstract class AnotherNeuronClass extends SomeNeuronClass {

    def now: Date
  }

  abstract class AnotherClass extends SomeNeuronInterface

  class Counter {

    var count: Int = _

    def increment: Counter = {
      count += 1
      this
    }
  }

  @Neuron
  trait HasDependency[T] {

    def get: T
  }
}
