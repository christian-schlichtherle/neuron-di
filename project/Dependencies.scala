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

import sbt._

object Dependencies {

  val ASM: ModuleID = "org.ow2.asm" % "asm" % "6.1"
  val Guice: ModuleID = {
    val version = sys.env.getOrElse("GUICE_VERSION", "4.2.0")
    val moduleID = "com.google.inject" % "guice" % version
    version match {
      // Exclude ASM 3.1 as a transitive dependency because it has a different group id and conflicts with ASM 5.2.
      // ASM 3.1 is a transitive dependency of Guice 3.0, which is used for continuous testing.
      case "3.0" => moduleID exclude("asm", "asm")
      case _ => moduleID
    }
  }
  val HamcrestLibrary: ModuleID = "org.hamcrest" % "hamcrest-library" % "1.3"
  def io(sbtVersion: String): ModuleID = "org.scala-sbt" % "io" % sbtVersion
  val Junit: ModuleID = "junit" % "junit" % "4.12"
  val JunitInterface: ModuleID = "com.novocode" % "junit-interface" % "0.11"
  val MockitoCore: ModuleID = "org.mockito" % "mockito-core" % "2.16.0"
  val MacroParadise: ModuleID = "org.scalamacros" % "paradise" % "2.1.+" cross CrossVersion.full
  val PlaySbtPlugin: ModuleID = "com.typesafe.play" % "sbt-plugin" % "2.6.5"
  val ScalaPlus: ModuleID = "global.namespace.scala-plus" %% "scala-plus" % "0.1"
  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion
  val Scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.5"

  val ScalaVersion_2_11: String = sys.env.getOrElse("SCALA_VERSION_2_11", "2.11.12")
  val ScalaVersion_2_12: String = sys.env.getOrElse("SCALA_VERSION_2_12", "2.12.5")
}
