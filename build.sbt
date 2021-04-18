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
import BuildSettings._
import Dependencies._

ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala, junit)
  .settings(ReleaseSettings, AggregateSettings)
  .settings(name := "Neuron DI")

lazy val core = project
  .settings(JavaLibrarySettings)
  .settings(
    inTask(assembly)(Seq(
      artifact ~= (_ withClassifier Some("shaded") withConfigurations Vector(Compile)),

      assemblyJarName := s"${normalizedName.value}-${version.value}-shaded.jar",

      // Relocate the classes and update references to them everywhere.
      assemblyShadeRules := Seq(
        ShadeRule.rename("org.objectweb.**" -> "global.namespace.neuron.di.internal.@1").inAll,
        ShadeRule.zap("module-info").inAll
      ),

      test := { }
    )),
    addArtifact(assembly / artifact, assembly),

    javacOptions += "-proc:none",
    libraryDependencies ++= Seq(
      ASM,
      HamcrestLibrary % Test,
      JUnitInterface % Test,
      Scalatest % Test,
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, major)) if major >= 13 => Seq(ScalaParallelCollections % Test)
        case _ => Seq.empty
      }
    },
    name := "Neuron DI for Java",
    normalizedName := "neuron-di",
  )

lazy val coreScala = project
  .in(file("core-scala"))
  .dependsOn(core)
  .settings(ScalaLibrarySettings)
  .settings(
    libraryDependencies ++= Seq(
      scalaReflect(scalaVersion.value),
      Scalatest % Test,
    ),
    name := "Neuron DI for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-scala",
  )

lazy val guice = project
  .dependsOn(core, junit % Test)
  .settings(JavaLibrarySettings)
  .settings(
    libraryDependencies ++= Seq(
      Guice,
      HamcrestLibrary % Test,
      JUnitInterface % Test,
      Scalatest % Test,
      ScalatestplusMockito % Test,
    ),
    name := "Neuron DI @ Guice for Java",
    normalizedName := "neuron-di-guice",
  )

lazy val guiceScala = project
  .in(file("guice-scala"))
  .dependsOn(guice, coreScala)
  .settings(ScalaLibrarySettings)
  .settings(
    libraryDependencies += Scalatest % Test,
    name := "Neuron DI @ Guice for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-guice-scala",
  )

lazy val junit = project
  .dependsOn(core)
  .settings(JavaLibrarySettings)
  .settings(
    libraryDependencies += JUnit,
    name := "Neuron DI @ JUnit",
    normalizedName := "neuron-di-junit",
  )
