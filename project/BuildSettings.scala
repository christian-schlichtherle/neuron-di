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
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object BuildSettings {

  def releaseSettings: Seq[Setting[_]] = {
    Seq(
      releaseCrossBuild := false,
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        releaseStepCommandAndRemaining("so test"),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepCommandAndRemaining("so publishSigned"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    )
  }

  private def commonSettings: Seq[Setting[_]] = {
    Seq(
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
      scmInfo := Some(ScmInfo(
        browseUrl = url("https://github.com/christian-schlichtherle/neuron-di"),
        connection = "scm:git:git://github.com/christian-schlichtherle/neuron-di.git",
        devConnection = Some("scm:git:ssh://git@github.com/christian-schlichtherle/neuron-di.git")
      ))
    )
  }

  def aggregateSettings: Seq[Setting[_]] = {
    commonSettings ++ Seq(
      autoScalaLibrary := false,
      crossPaths := false,
      publishArtifact := false
    )
  }

  def artifactSettings: Seq[Setting[_]] = {
    commonSettings ++ inConfig(Test)(Seq(
      fork := true, // triggers `javaOptions`
      javaOptions += "-ea"
    )) ++ Seq(
      dependencyOverrides += JUnit,
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
    )
  }

  def librarySettings: Seq[Setting[_]] = artifactSettings

  def javaLibrarySettings: Seq[Setting[_]] = {
    librarySettings ++ Seq(
      autoScalaLibrary := false,
      crossPaths := false,
      javacOptions := DefaultOptions.javac ++ Seq(Opts.compile.deprecation, "-g"),
      javacOptions in doc := DefaultOptions.javac
    )
  }

  def scalaLibrarySettings: Seq[Setting[_]] = {
    librarySettings ++ Seq(
      addCompilerPlugin(MacroParadise),
      compileOrder := CompileOrder.JavaThenScala,
      crossPaths := true,
      crossScalaVersions := Seq(ScalaVersion_2_11, ScalaVersion_2_12),
      scalacOptions := DefaultOptions.scalac ++ Seq(Opts.compile.deprecation, Opts.compile.explaintypes, "-feature", Opts.compile.unchecked),
      scalaVersion := ScalaVersion_2_12
    )
  }

  def pluginSettings: Seq[Setting[_]] = artifactSettings

  def sbtPluginSettings: Seq[Setting[_]] = {
    pluginSettings ++ Seq(
      crossPaths := false,
      sbtPlugin := true,
      scalacOptions := DefaultOptions.scalac ++ Seq(Opts.compile.deprecation, Opts.compile.explaintypes, "-feature", Opts.compile.unchecked)
    )
  }

  private lazy val ScalaVersion_2_11 = sys.props.getOrElse("SCALA_VERSION_2.11", "2.11.11")
  private lazy val ScalaVersion_2_12 = sys.props.getOrElse("SCALA_VERSION_2.12", "2.12.2")
}
