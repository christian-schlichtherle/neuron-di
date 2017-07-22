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

  val ASM: ModuleID = "org.ow2.asm" % "asm" % "5.2"
  val Guice: ModuleID = "com.google.inject" % "guice" % sys.env.getOrElse("GUICE_VERSION", "3.0")
  val HamcrestLibrary: ModuleID = "org.hamcrest" % "hamcrest-library" % "1.3"
  val IO: ModuleID = "org.scala-sbt" % "io" % "0.13.15"
  val JUnit: ModuleID = "junit" % "junit" % "4.12"
  val JUnitInterface: ModuleID = "com.novocode" % "junit-interface" % "0.11"
  val MockitoCore: ModuleID = "org.mockito" % "mockito-core" % "2.8.47"
  val MacroParadise: ModuleID = "org.scalamacros" % "paradise" % "2.1.+" cross CrossVersion.full
  val PlaySbtPlugin: ModuleID = "com.typesafe.play" % "sbt-plugin" % "2.6.2"
  val ScalaPlus: ModuleID = "global.namespace.scala-plus" %% "scala-plus" % "0.1"
  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion
  val ScalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.3"
}
