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

import scala.xml._

lazy val mavenProject: SettingKey[NodeSeq] = SettingKey[NodeSeq]("maven-project", "The <project> element of a maven POM.")

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala)
  .settings(
    inThisBuild(Seq(
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      compileOrder := CompileOrder.JavaThenScala,
      fork := true, // required to make `javaOptions` effective.
      javacOptions in compile := javacOptions.value ++ Seq("-target", "1.8", "-deprecation"),
      javacOptions := Seq("-source", "1.8"), // unfortunately, this is used for running javadoc, e.g. in the `packageDoc` task key?!
      javaOptions += "-ea",
      homepage := Some(url("https://github.com/christian-schlichtherle/neuron-di")),
      licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
      mavenProject := XML.loadFile(baseDirectory.value + "/pom.xml"),
      organization := "global.namespace.neuron-di",
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
      // Selectively publish Scala artifacts only at this time as long as sbt-assembly doesn't support Java 8.
      scalacOptions := Seq("-deprecation", "-explaintypes", "-feature", "-unchecked"),
      scalaVersion := (mavenProject.value \ "properties" \ "scala.version").text,
      scmInfo := Some(ScmInfo(
        browseUrl = url("https://github.com/christian-schlichtherle/neuron-di"),
        connection = "scm:git:git://github.com/christian-schlichtherle/neuron-di.git",
        devConnection = Some("scm:git:ssh://git@github.com/christian-schlichtherle/neuron-di.git")
      )),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a"),
      version := (mavenProject.value \ "version").text
    )),
    name := "Neuron DI Parent",
    publishArtifact := false
  )

lazy val core = project
  .in(file("core"))
  .settings(
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      asm,
      hamcrestLibrary % Test,
      junit % "provided, optional",
      junitInterface % "test, optional",
      scalaTest % Test
    ),
    name := "Neuron DI for Java",
    normalizedName := "neuron-di",
    publishArtifact := false // sbt-assembly plugin 0.14.3 doesn't support shading Java 8 byte code
  )

lazy val coreScala = project
  .in(file("core-scala"))
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      scalaReflect % scalaVersion.value,
      scalaTest % Test
    ),
    name := "Neuron DI for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-scala"
  )

lazy val guice = project
  .in(file("guice"))
  .dependsOn(core)
  .settings(
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      Dependencies.guice,
      hamcrestLibrary % Test,
      junit % Test,
      junitInterface % "test, optional",
      mockitoCore % Test,
      scalaTest % Test
    ),
    name := "Neuron DI @ Guice for Java",
    normalizedName := "neuron-di-guice",
    publishArtifact := false
  )

lazy val guiceScala = project
  .in(file("guice-scala"))
  .dependsOn(guice, coreScala)
  .settings(
    libraryDependencies += scalaTest % Test,
    name := "Neuron DI @ Guice for Scala " + scalaBinaryVersion.value,
    normalizedName := "neuron-di-guice-scala"
  )
