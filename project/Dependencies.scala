/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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

  val ASM: ModuleID = "org.ow2.asm" % "asm" % "7.2"
  val Guice: ModuleID = "com.google.inject" % "guice" % "4.2.2"
  val HamcrestLibrary: ModuleID = "org.hamcrest" % "hamcrest-library" % "1.3"
  val JUnit: ModuleID = "junit" % "junit" % "4.12"
  val JUnitInterface: ModuleID = "com.novocode" % "junit-interface" % "0.11"
  val MockitoCore: ModuleID = "org.mockito" % "mockito-core" % "3.1.0"
  val MacroParadise: ModuleID = "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full

  val ScalaParallelCollections: ModuleID = "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"

  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion

  val ScalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.8"

  val ScalaVersion_2_11: String = sys.env.getOrElse("SCALA_VERSION_2_11", "2.11.12")
  val ScalaVersion_2_12: String = sys.env.getOrElse("SCALA_VERSION_2_12", "2.12.10")
  val ScalaVersion_2_13: String = sys.env.getOrElse("SCALA_VERSION_2_13", "2.13.1")
}
