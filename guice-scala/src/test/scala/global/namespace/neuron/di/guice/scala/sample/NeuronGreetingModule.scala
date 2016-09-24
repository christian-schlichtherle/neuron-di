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
package global.namespace.neuron.di.guice.scala.sample

import javax.inject.Singleton

import global.namespace.neuron.di.guice.scala._
import global.namespace.neuron.di.scala.Neuron

import scala.annotation.meta.getter

@Neuron
trait NeuronGreeting extends Greeting {

  val _formatter: Formatter

  def message(entity: String): String = _formatter format entity
}

@Neuron
trait NeuronFormatter extends Formatter {

  private type Named = javax.inject.Named @getter

  @Named("format") val _format: String

  def format(args: AnyRef*): String = String.format(_format, args: _*)
}

class NeuronGreetingModule extends NeuronModule {

  def configure() {
    bindClass[Greeting].toNeuronClass[NeuronGreeting].inScope[Singleton]
    bindClass[Formatter].toNeuronClass[NeuronFormatter]
    bindConstantNamed("format").to("Hello %s!")
  }
}
