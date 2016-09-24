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

import javax.inject.Singleton

import global.namespace.neuron.di.guice.sample.{Bar, BarImpl, Foo, FooImpl}
import global.namespace.neuron.di.guice.scala._

class FooBarModule extends NeuronModule {

  def configure() {
    bindConstantNamed("one").to(1)
    bindClass[Foo]
      .named("impl")
      .toClass[FooImpl]
      .inScope[Singleton]
    bindClass[Bar].toClass[BarImpl]
  }
}
