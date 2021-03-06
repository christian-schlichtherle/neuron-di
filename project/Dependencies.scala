/*
 * Copyright © 2016 - 2020 Schlichtherle IT Services
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

  val ASM: ModuleID = "org.ow2.asm" % "asm" % "9.1"
  val Guice: ModuleID = {
    val version = sys.env.getOrElse("GUICE_VERSION", "5.0.1")
    val moduleID = "com.google.inject" % "guice" % version
    version match {
      // Exclude ASM 3.1 as a transitive dependency because it has a different group id and conflicts with ASM 7.
      // ASM 3.1 is a transitive dependency of Guice 3.0, which is used by continuous testing.
      case "3.0" => moduleID exclude("asm", "asm")
      case _ => moduleID
    }
  }
  val HamcrestLibrary: ModuleID = "org.hamcrest" % "hamcrest-library" % "1.3"
  val JUnit: ModuleID = "junit" % "junit" % "4.13.2"
  val JUnitInterface: ModuleID = "com.novocode" % "junit-interface" % "0.11"
  val Paradise: ModuleID = "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full

  val ScalaParallelCollections: ModuleID = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.2"

  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion

  val Scalatest = "org.scalatest" %% "scalatest" % "3.2.7"
  val ScalatestplusMockito = "org.scalatestplus" %% "mockito-3-4" % "3.2.7.0"

  val ScalaVersion_2_11: String = sys.env.getOrElse("SCALA_VERSION_2_11", "2.11.12")
  val ScalaVersion_2_12: String = sys.env.getOrElse("SCALA_VERSION_2_12", "2.12.13")
  val ScalaVersion_2_13: String = sys.env.getOrElse("SCALA_VERSION_2_13", "2.13.5")
}
