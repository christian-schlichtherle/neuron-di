/*
 * Copyright © 2009-2017 Lightbend Inc. <https://www.lightbend.com>
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
package global.namespace.neuron.di.play.plugin

import java.io.File
import java.util.Locale

import global.namespace.neuron.di.play.plugin.NeuronRoutesGenerator._
import play.routes.compiler.RoutesCompiler.RoutesCompilerTask
import play.routes.compiler.{InjectedRoutesGenerator => delegate, _}

/** @author Christian Schlichtherle */
object NeuronRoutesGenerator {

  case class Dependency[+T <: Rule](ident: String, clazz: String, rule: T)

  def apply(identifier: (String, String) => String = defaultIdentifier) = new NeuronRoutesGenerator(identifier)

  def defaultIdentifier(packageName: String, className: String): String = {
    val strippedPackageName = packageName.stripPrefix("controllers").stripPrefix(".")
    if (strippedPackageName.isEmpty) {
      className.span(_.isUpper) match {
        case (prefix, suffix) => prefix.toLowerCase(Locale.ROOT) + suffix
      }
    } else {
      strippedPackageName.replace('.', '_') + "_" + className
    }
  }
}

/** @author Christian Schlichtherle */
class NeuronRoutesGenerator(identifier: (String, String) => String) extends RoutesGenerator {

  def generate(task: RoutesCompilerTask, namespace: Option[String], rules: List[Rule]): Seq[(String, String)] = {
    delegate.generate(task.copy(forwardsRouter = false), namespace, rules) ++ {
      if (task.forwardsRouter) {
        val folder = namespace.map(_.replace('.', '/')).getOrElse("")
        val sourceInfo = RoutesSourceInfo(task.file.getCanonicalPath.replace(File.separator, "/"), new java.util.Date().toString)
        Seq(folder + "/Routes.scala" -> generateRouter(sourceInfo, namespace, task.additionalImports, rules))
      } else {
        Nil
      }
    }
  }

  // Copied and adapted from `play.routes.compiler.InjectecRoutesGenerator` - kudos!
  private def generateRouter(sourceInfo: RoutesSourceInfo, namespace: Option[String], additionalImports: Seq[String], rules: List[Rule]) = {

    // Generate dependency descriptors for all includes
    val includesDeps = {
      rules.collect {
        case include: Include => include
      }.groupBy(_.router).zipWithIndex.map {
        case ((router, includes), index) =>
          router -> Dependency(router.replace('.', '_') + "_" + index, router, includes.head)
      }
    }

    // Generate dependency descriptors for all routes
    val routesDeps = {
      rules
        .collect { case route: Route => route }
        .groupBy(r => (r.call.packageName, r.call.controller, r.call.instantiate))
        .map { case (key@(packageName, className, instantiate), routes) =>
          val ident = identifier(packageName, className)
          val clazz = packageName + "." + className
          // If it's using the @ syntax, we depend on the provider (ie, look it up each time)
          val dep = if (instantiate) s"javax.inject.Provider[$clazz]" else clazz
          key -> Dependency(ident, dep, routes.head)
        }
    }

    // Get the distinct dependency descriptors in the same order as defined in the routes file
    val orderedDeps = {
      rules.map {
        case include: Include =>
          includesDeps(include.router)
        case route: Route =>
          routesDeps((route.call.packageName, route.call.controller, route.call.instantiate))
      }.distinct
    }

    // Map all the rules to dependency descriptors
    val rulesWithDeps = {
      rules.map {
        case include: Include =>
          includesDeps(include.router).copy(rule = include)
        case route: Route =>
          routesDeps((route.call.packageName, route.call.controller, route.call.instantiate)).copy(rule = route)
      }
    }

    twirl.forwardsRouter(
      sourceInfo,
      namespace,
      additionalImports,
      orderedDeps,
      rulesWithDeps,
      includesDeps.values.toSeq
    ).body
  }

  def id: String = "neuron"
}
