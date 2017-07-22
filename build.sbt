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
import BuildTools._
import Dependencies._

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala, sbtPlugin, playPlugin)
  .settings(aggregateSettings: _*)

lazy val core = project
  .in(file("core"))
  .settings(javaLibrarySettings: _*)
  .settings(
    inTask(assembly)(Seq(
      artifact in Compile := {
        (artifact in Compile).value.copy(configurations = Seq(Compile))
      },
      // sbt-assembly 0.14.5 doesn't understand combined dependency configurations like `JUnit % "provided, optional"`.
      // So JUnit and it's transitive dependencies need to be manually excluded.
      assemblyExcludedJars := {
        (externalDependencyClasspath in assembly).value filter { attributedFile =>
          val fileName = attributedFile.data.getName
          fileName.startsWith("junit-") || fileName.startsWith("hamcrest-core-")
        }
      },
      assemblyJarName := s"${normalizedName.value}-${version.value}.jar",
      assemblyShadeRules := Seq(
        ShadeRule.rename("org.objectweb.**" -> "global.namespace.neuron.di.internal.@1").inLibrary(ASM)
      ),
      test := {}
    )),
    addArtifact(artifact in (Compile, assembly), assembly),
    artifact in (Compile, packageBin) := {
      (artifact in (Compile, packageBin)).value.copy(classifier = Some("classes"))
    },
    javacOptions += "-proc:none",
    libraryDependencies ++= Seq(
      ASM % "compile, optional",
      HamcrestLibrary % Test,
      JUnit % "provided, optional",
      JUnitInterface % Test,
      ScalaTest % Test
    ),
    name := "Neuron DI for Java",
    normalizedName := "neuron-di"
  )

lazy val coreScala = project
  .in(file("core-scala"))
  .dependsOn(core)
  .settings(scalaLibrarySettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      scalaReflect(scalaVersion.value),
      ScalaTest % Test
    ),
    name := "Neuron DI for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-scala"
  )

lazy val guice = project
  .in(file("guice"))
  .dependsOn(core)
  .settings(javaLibrarySettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Guice,
      HamcrestLibrary % Test,
      JUnitInterface % Test,
      MockitoCore % Test,
      ScalaTest % Test
    ),
    name := "Neuron DI @ Guice for Java",
    normalizedName := "neuron-di-guice"
  )

lazy val guiceScala = project
  .in(file("guice-scala"))
  .dependsOn(guice, coreScala)
  .settings(scalaLibrarySettings: _*)
  .settings(
    libraryDependencies += ScalaTest % Test,
    name := "Neuron DI @ Guice for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-guice-scala"
  )

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .settings(sbtPluginSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.IO,
      ScalaPlus
    ),
    name := "Neuron DI SBT Plugin",
    normalizedName := "neuron-di-sbt-plugin",
    resourceGenerators in Compile += generateVersionFile.taskValue
  )

lazy val playPlugin = project
  .in(file("play-plugin"))
  .dependsOn(sbtPlugin)
  .enablePlugins(SbtTwirl)
  .settings(sbtPluginSettings: _*)
  .settings(
    addSbtPlugin(PlaySbtPlugin % Provided),
    name := "Neuron DI Play Plugin",
    normalizedName := "neuron-di-play-plugin",
    TwirlKeys.templateFormats := Map("twirl" -> "play.routes.compiler.ScalaFormat")
  )
