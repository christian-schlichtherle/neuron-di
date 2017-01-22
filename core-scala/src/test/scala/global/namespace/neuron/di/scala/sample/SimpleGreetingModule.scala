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

import global.namespace.neuron.di.scala._

@Neuron
trait SimpleGreeting {

  def formatter: SimpleFormatter

  /** Returns a greeting message for the given entity. */
  def message(entity: String): String = formatter format entity
}

@Neuron
trait SimpleFormatter {

  def theFormat: String

  /** Returns a text which has been formatted using the given arguments. */
  def format(args: AnyRef*): String = String.format(theFormat, args: _*)
}

object SimpleGreetingModule {

  lazy val greeting: SimpleGreeting = wire[SimpleGreeting]

  lazy val formatter: SimpleFormatter = wire[SimpleFormatter]

  val theFormat = "Hello %s!"
}
