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
package global.namespace.neuron.di.sbt.plugin

import global.namespace.scala.plus.ResourceLoan._
import sbt._

/** @author Christian Schlichtherle */
object Dependencies {

  // Allow for some limited variance of this dependency using '+' as the increment version number:
  // As of this writing, Macro Paradise 2.1.1 is the latest release, but hasn't been back-ported to all releases of
  // Scala 2.11.x and Scala 2.12.x.
  // For example, for Scala 2.11.0 and Scala 2.12.0, only Macro Paradise 2.1.0 is available, whereas for Scala 2.11.11
  // and Scala 2.12.2, Macro Paradise 2.1.1 is available.
  val MacroParadise: ModuleID = "org.scalamacros" % "paradise" % "2.1.+" cross CrossVersion.full

  private val NeuronDIVersion = loan(getClass.getResourceAsStream("version")).to(IO.readStream(_))

  val NeuronDIForJava: ModuleID = component("neuron-di")
  val NeuronDIForScala: ModuleID = component("neuron-di-scala")
  val NeuronDIAtGuiceForJava: ModuleID = component("neuron-di-guice")
  val NeuronDIAtGuiceForScala: ModuleID = component("neuron-di-guice-scala")

  private def component(id: String) = "global.namespace.neuron-di" %% id % NeuronDIVersion
}
