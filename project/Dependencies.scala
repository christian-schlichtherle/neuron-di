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

  val asm: ModuleID = "org.ow2.asm" % "asm" % "5.2"
  val guice: ModuleID = "com.google.inject" % "guice" % sys.env.getOrElse("GUICE_VERSION", "3.0")
  val hamcrestLibrary: ModuleID = "org.hamcrest" % "hamcrest-library" % "1.3"
  val junit: ModuleID = "junit" % "junit" % "4.12"
  val junitInterface: ModuleID = "com.novocode" % "junit-interface" % "0.11"
  val mockitoCore: ModuleID = "org.mockito" % "mockito-core" % "2.8.47"
  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion
  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.3"
  val paradise: ModuleID = "org.scalamacros" % "paradise" % sys.env.getOrElse("PARADISE_VERSION", "2.1.1") cross CrossVersion.full
}
