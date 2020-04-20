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
package global.namespace.neuron.di.scala

import scala.reflect.macros.blackbox

private trait MacroAnnotation extends LowPriorityMacroAnnotation {

  val c: blackbox.Context

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree

  def scala2javaCachingStrategy(arg: Tree): Tree = {
    q"_root_.global.namespace.neuron.di.java.CachingStrategy.${
      arg.collect {
        case Select(_, strategy: TermName) => strategy
        case Ident(strategy: TermName) => strategy
      }.head
    }"
  }

  lazy val newCachingAnnotationTerm: Tree =
    q"new _root_.global.namespace.neuron.di.java.Caching @_root_.scala.annotation.meta.getter"

  lazy val newNeuronAnnotationTerm =
    q"new _root_.global.namespace.neuron.di.java.Neuron"

  // Matching Scala 2.11.12, 2.12.8 and 2.13.0:
  private lazy val allFlags = Set(
    ABSOVERRIDE,
    ABSTRACT,
    ARTIFACT,
    BYNAMEPARAM,
    CASE,
    CASEACCESSOR,
    CONTRAVARIANT,
    COVARIANT,
    DEFAULTINIT,
    DEFAULTPARAM,
    DEFERRED,
    ENUM,
    FINAL,
    IMPLICIT,
    INTERFACE,
    LAZY,
    LOCAL,
    MACRO,
    MUTABLE,
    OVERRIDE,
    PARAM,
    PARAMACCESSOR,
    PRESUPER,
    PRIVATE,
    PROTECTED,
    SEALED,
    STABLE,
    SYNTHETIC,
    TRAIT
  )

  implicit class FlagOps(left: FlagSet) {

    def &~(right: FlagSet): FlagSet = {
      var result = NoFlags
      for (flag <- allFlags) {
        if (left == (left | flag) && right != (right | flag)) {
          result |= flag
        }
      }
      result
    }
  }

  implicit def position: Position = c.enclosingPosition

  def info(msg: String, force: Boolean = false)(implicit pos: Position): Unit = c.info(pos, msg, force)

  def warn(msg: String)(implicit pos: Position): Unit = c.warning(pos, msg)

  def error(msg: String)(implicit pos: Position): Unit = c.error(pos, msg)

  def abort(msg: String)(implicit pos: Position): Nothing = c.abort(pos, msg)

  def isCachingAnnotation(annotation: Tree): Boolean = {
    global.namespace.neuron.di.scala.isCachingAnnotation(c)(annotation)
  }

  def isNeuronAnnotation(annotation: Tree): Boolean = {
    global.namespace.neuron.di.scala.isNeuronAnnotation(c)(annotation)
  }
}
