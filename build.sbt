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

import Dependencies._
import SbtPluginTools._

import scala.xml._

lazy val mavenProject: SettingKey[NodeSeq] = SettingKey[NodeSeq]("maven-project", "The <project> element of a maven POM.")

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala, sbtPlugin)
  .settings(
    inThisBuild(Seq(
      compileOrder := CompileOrder.JavaThenScala,
      crossPaths := false,
      fork in Test := true, // required to make `javaOptions` effective.
      javacOptions := DefaultOptions.javac ++ Seq(Opts.compile.deprecation, "-g"),
      javacOptions in doc := DefaultOptions.javac,
      javaOptions += "-ea",
      homepage := Some(url("https://github.com/christian-schlichtherle/neuron-di")),
      licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
      mavenProject := XML.loadFile(baseDirectory.value + "/pom.xml"),
      organization := "global.namespace.neuron-di",
      organizationHomepage := Some(new URL("http://schlichtherle.de")),
      organizationName := "Schlichtherle IT Services",
      pomExtra := mavenProject.value \ "developers",
      pomIncludeRepository := (_ => false),
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        Some(
          if (version(_ endsWith "-SNAPSHOT").value) {
            "snapshots" at nexus + "content/repositories/snapshots"
          } else {
            "releases" at nexus + "service/local/staging/deploy/maven2"
          }
        )
      },
      scalacOptions := DefaultOptions.scalac ++ Seq(Opts.compile.deprecation, Opts.compile.explaintypes, Opts.compile.unchecked, "-feature"),
      scalaVersion := (mavenProject.value \ "properties" \ "scala.version").text,
      scmInfo := Some(ScmInfo(
        browseUrl = url("https://github.com/christian-schlichtherle/neuron-di"),
        connection = "scm:git:git://github.com/christian-schlichtherle/neuron-di.git",
        devConnection = Some("scm:git:ssh://git@github.com/christian-schlichtherle/neuron-di.git")
      )),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a"),
      version := (mavenProject.value \ "version").text
    )),
    name := "Neuron DI",
    publishArtifact := false
  )

lazy val core = project
  .in(file("core"))
  .settings(
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      ASM,
      HamcrestLibrary % Test,
      JUnit % "provided, optional",
      JUnitInterface % "test, optional",
      ScalaTest % Test
    ),
    name := "Neuron DI for Java",
    normalizedName := "neuron-di",
    publishArtifact := false // built by Maven because the sbt-assembly plugin 0.14.3 doesn't support shading Java 8 byte code
  )

lazy val coreScala = project
  .in(file("core-scala"))
  .dependsOn(core)
  .settings(
    addCompilerPlugin(MacroParadise),
    crossPaths := true,
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
  .settings(
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      Guice,
      HamcrestLibrary % Test,
      JUnit % Test,
      JUnitInterface % "test, optional",
      MockitoCore % Test,
      ScalaTest % Test
    ),
    name := "Neuron DI @ Guice for Java",
    normalizedName := "neuron-di-guice",
    publishArtifact := false // built by Maven
  )

lazy val guiceScala = project
  .in(file("guice-scala"))
  .dependsOn(guice, coreScala)
  .settings(
    addCompilerPlugin(MacroParadise),
    crossPaths := true,
    libraryDependencies += ScalaTest % Test,
    name := "Neuron DI @ Guice for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-guice-scala"
  )

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.IO,
      ScalaPlus
    ),
    name := "Neuron DI SBT Plugin",
    normalizedName := "neuron-di-sbt-plugin",
    resourceGenerators in Compile += generateVersionFile.taskValue,
    Keys.sbtPlugin := true,
    scalaVersion := "2.10.6"
  )
