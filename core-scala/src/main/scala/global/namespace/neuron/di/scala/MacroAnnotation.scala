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
package global.namespace.neuron.di.scala

import global.namespace.neuron.di.scala.MacroAnnotation._

import scala.reflect.macros.blackbox

/** @author Christian Schlichtherle */
private trait MacroAnnotation {

  protected val c: blackbox.Context

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree

  protected def scala2javaCachingStrategy(arg: Tree): Tree = {
    q"_root_.global.namespace.neuron.di.java.CachingStrategy.${
      arg match {
        case q"$prefix.$name" => name
        case other => TermName(other.toString)
      }
    }"
  }

  protected lazy val newCachingAnnotationTerm: Tree =
    q"new _root_.global.namespace.neuron.di.java.Caching @_root_.scala.annotation.meta.getter"

  protected lazy val newNeuronAnnotationTerm =
    q"new _root_.global.namespace.neuron.di.java.Neuron"

  protected implicit class FlagOps(left: FlagSet) {

    // As of Scala 2.11.8:
    private val allFlags = Set(
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

    def &~(right: FlagSet): FlagSet = {
      var result = NoFlags
      for (flag <- allFlags) {
        if (flag == (left | flag) && flag != (right | flag)) {
          result |= flag
        }
      }
      result
    }
  }

  protected implicit def position: Position = c.enclosingPosition

  protected def info(msg: String, force: Boolean = false)(implicit pos: Position): Unit = c.info(pos, msg, force)

  protected def warning(msg: String)(implicit pos: Position): Unit = c.warning(pos, msg)

  protected def error(msg: String)(implicit pos: Position): Unit = c.error(pos, msg)

  protected def abort(msg: String)(implicit pos: Position): Nothing = c.abort(pos, msg)

  protected def isCachingAnnotation(tree: Tree): Boolean =
    tpeName(tree).exists(name => name == javaCachingAnnotationName || name == scalaCachingAnnotationName)

  protected def isNeuronAnnotation(tree: Tree): Boolean =
    tpeName(tree).exists(name => name == javaNeuronAnnotationName || name == scalaNeuronAnnotationName)

  private def tpeName(tree: Tree) = Option(tree.tpe).map(_.toString)
}

/** @author Christian Schlichtherle */
private object MacroAnnotation {

  import global.namespace.neuron.di._

  private val javaCachingAnnotationName = classOf[java.Caching].getName
  private val javaNeuronAnnotationName = classOf[java.Neuron].getName
  private val scalaCachingAnnotationName = classOf[scala.Caching].getName
  private val scalaNeuronAnnotationName = classOf[scala.Neuron].getName
}
