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

lazy val root = project
  .in(file("."))
  .aggregate(core, coreScala, guice, guiceScala, sbtPlugin)
  .settings(
    inThisBuild(Seq(
      compileOrder := CompileOrder.JavaThenScala,
      crossScalaVersions := Seq("2.11.0", "2.12.0"),
      crossPaths := false,
      dependencyOverrides += JUnit,
      fork in Test := true, // required to make `javaOptions` effective.
      javacOptions := DefaultOptions.javac ++ Seq(Opts.compile.deprecation, "-g"),
      javacOptions in doc := DefaultOptions.javac,
      javaOptions += "-ea",
      homepage := Some(url("https://github.com/christian-schlichtherle/neuron-di")),
      licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
      organization := "global.namespace.neuron-di",
      organizationHomepage := Some(new URL("http://schlichtherle.de")),
      organizationName := "Schlichtherle IT Services",
      pomExtra := {
        <developers>
          <developer>
            <name>Christian Schlichtherle</name>
            <email>christian AT schlichtherle DOT de</email>
            <organization>Schlichtherle IT Services</organization>
            <timezone>Europe/Berlin</timezone>
            <roles>
              <role>owner</role>
            </roles>
            <properties>
              <picUrl>http://www.gravatar.com/avatar/e2f69ddc944f8891566fc4b18518e4e6.png</picUrl>
            </properties>
          </developer>
        </developers>
      },
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
      scalacOptions := DefaultOptions.scalac ++ Seq(Opts.compile.deprecation, Opts.compile.explaintypes, "-feature", Opts.compile.unchecked),
      scalaVersion := "2.11.11",
      scmInfo := Some(ScmInfo(
        browseUrl = url("https://github.com/christian-schlichtherle/neuron-di"),
        connection = "scm:git:git://github.com/christian-schlichtherle/neuron-di.git",
        devConnection = Some("scm:git:ssh://git@github.com/christian-schlichtherle/neuron-di.git")
      )),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
      version := "5.2-SNAPSHOT"
    )),
    name := "Neuron DI",
    publishArtifact := false
  )

lazy val core = project
  .in(file("core"))
  .settings(
    addArtifact(artifact in (Compile, assembly), assembly),
    artifact in (Compile, packageBin) := {
      (artifact in (Compile, packageBin)).value.copy(classifier = Some("classes"))
    },
    artifact in (Compile, assembly) := {
      (artifact in (Compile, assembly)).value.copy(configurations = Seq(Compile))
    },
    // sbt-assembly 0.14.5 doesn't understand combined dependency configurations like `JUnit % "provided, optional"`.
    // So JUnit and it's transitive dependencies need to be manually excluded.
    assemblyExcludedJars in assembly := {
      (externalDependencyClasspath in assembly).value filter { attributedFile =>
        val fileName = attributedFile.data.getName
        fileName.startsWith("junit-") || fileName.startsWith("hamcrest-core-")
      }
    },
    assemblyJarName in assembly := s"${normalizedName.value}-${version.value}.jar",
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("org.objectweb.**" -> "global.namespace.neuron.di.internal.@1").inLibrary(ASM)
    ),
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      ASM % "compile, optional",
      HamcrestLibrary % Test,
      JUnit % "provided, optional",
      JUnitInterface % Test,
      ScalaTest % Test
    ),
    name := "Neuron DI for Java",
    normalizedName := "neuron-di",
    test in assembly := {}
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
