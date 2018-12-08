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

import BuildSettings._
import Dependencies._

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala, junit)
  .settings(releaseSettings)
  .settings(aggregateSettings)
  .settings(name := "Neuron DI")

lazy val core = project
  .in(file("core"))
  .settings(javaLibrarySettings)
  .settings(
    inTask(assembly)(Seq(
      artifact ~= (_ withClassifier Some("shaded") withConfigurations Vector(Compile)),

      assemblyJarName := s"${normalizedName.value}-${version.value}-shaded.jar",

      // Relocate the classes and update references to them everywhere.
      assemblyShadeRules := Seq(
        ShadeRule.rename("org.objectweb.**" -> "global.namespace.neuron.di.internal.@1").inAll
      ),

      test := { }
    )),
    addArtifact(artifact in assembly, assembly),

    javacOptions += "-proc:none",
    libraryDependencies ++= Seq(
      ASM,
      HamcrestLibrary % Test,
      JUnitInterface % Test,
      Scalatest % Test
    ),
    name := "Neuron DI for Java",
    normalizedName := "neuron-di"
  )

lazy val coreScala = project
  .in(file("core-scala"))
  .dependsOn(core)
  .settings(scalaLibrarySettings)
  .settings(
    libraryDependencies ++= Seq(
      scalaReflect(scalaVersion.value),
      Scalatest % Test
    ),
    name := "Neuron DI for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-scala"
  )

lazy val guice = project
  .in(file("guice"))
  .dependsOn(core, junit % Test)
  .settings(javaLibrarySettings)
  .settings(
    libraryDependencies ++= Seq(
      Guice,
      HamcrestLibrary % Test,
      JUnitInterface % Test,
      MockitoCore % Test,
      Scalatest % Test
    ),
    name := "Neuron DI @ Guice for Java",
    normalizedName := "neuron-di-guice"
  )

lazy val guiceScala = project
  .in(file("guice-scala"))
  .dependsOn(guice, coreScala)
  .settings(scalaLibrarySettings)
  .settings(
    libraryDependencies += Scalatest % Test,
    name := "Neuron DI @ Guice for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-guice-scala"
  )

lazy val junit = project
  .in(file("junit"))
  .dependsOn(core)
  .settings(javaLibrarySettings)
  .settings(
    libraryDependencies += JUnit,
    name := "Neuron DI JUnit",
    normalizedName := "neuron-di-junit"
  )
