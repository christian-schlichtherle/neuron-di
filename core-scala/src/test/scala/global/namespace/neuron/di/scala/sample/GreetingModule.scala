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
package global.namespace.neuron.di.scala.sample

import global.namespace.neuron.di.scala.{Incubator, Neuron}

trait Greeting {

  /** Returns a greeting message for the given entity. */
  def message(entity: String): String
}

@Neuron
trait RealGreeting extends Greeting {

  val _formatter: Formatter

  def message(entity: String): String = _formatter format entity
}

trait Formatter {

  /** Returns a text which has been formatted using the given arguments. */
  def format(args: AnyRef*): String
}

@Neuron
trait RealFormatter extends Formatter {

  val _format: String

  def format(args: AnyRef*): String = String.format(_format, args: _*)
}

object GreetingModule {

  lazy val greeting: Greeting = Incubator
    .stub[RealGreeting]
    .bind(_._formatter).to(formatter)
    .breed

  lazy val formatter: Formatter = Incubator
    .stub[RealFormatter]
    .bind(_._format).to("Hello %s!")
    .breed
}
