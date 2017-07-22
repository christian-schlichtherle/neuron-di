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

import global.namespace.neuron.di.sbt.plugin.Dependencies._
import sbt.Keys._
import sbt._

object MacroParadisePlugin extends AutoPlugin {

  override def projectSettings: Seq[Def.Setting[_]] = {
    Seq(addCompilerPlugin(MacroParadise))
  }
}

object NeuronDIPlugin extends AutoPlugin {

  val autoImport: Dependencies.type = Dependencies
}

object NeuronDIForJavaPlugin extends AutoPlugin {

  override def requires: Plugins = NeuronDIPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies += NeuronDIForJava)
}

object NeuronDIForScalaPlugin extends AutoPlugin {

  override def requires: Plugins = NeuronDIPlugin && MacroParadisePlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies += NeuronDIForScala)
}

object NeuronDIAtGuiceForJavaPlugin extends AutoPlugin {

  override def requires: Plugins = NeuronDIPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies += NeuronDIAtGuiceForJava)
}

object NeuronDIAtGuiceForScalaPlugin extends AutoPlugin {

  override def requires: Plugins = NeuronDIPlugin && MacroParadisePlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies += NeuronDIAtGuiceForScala)
}
