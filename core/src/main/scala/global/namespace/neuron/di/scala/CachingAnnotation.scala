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

import scala.reflect.macros.blackbox

private class CachingAnnotation(val c: blackbox.Context) extends MacroAnnotation {

  import c.universe._
  import Flag._

  protected def apply0(inputs: List[c.Tree]): c.Tree = {
    val outputs = inputs match {
      case DefDef(mods, name, tparams, vparamss, tpt, rhs) :: rest =>
        val owner = c.internal.enclosingOwner
        if (!owner.annotations.exists(a => isNeuronAnnotation(a.tree))) {
          error("A caching definition must be a member of a neuron class or interface...")
          error("... but there is no @Neuron annotation here.")(owner.pos)
        }
        if (mods hasFlag MACRO) {
          abort("A caching definition cannot be a macro.")
        }
        if (mods hasFlag FINAL) {
          error("A caching definition cannot be final.")
        }
        if (mods hasFlag PRIVATE) {
          error("A caching definition cannot be private.")
        }
        vparamss match {
          case Nil | List(Nil) =>
          case _ =>
            abort("A caching definition cannot have parameters.")
        }
        typeOf(rhs) match {
          case ConstantType(_) =>
            error("A caching method cannot return a constant.")(tpt.pos)
          case TypeRef(_, sym, _)
            if sym == c.symbolOf[Unit] || sym == c.symbolOf[Nothing] =>
            error("A caching method cannot return Unit or Nothing.")(tpt.pos)
          case _ =>
        }
        val term = c.prefix.tree match {
          case Apply(fun, args) =>
            val Apply(fun2, _) = newCachingAnnotationTerm
            Apply(fun2, args map {
              case q"value = ${tree: Tree}" => scala2javaCachingStrategy(tree)
              case tree => scala2javaCachingStrategy(tree)
            })
        }
        DefDef(mods.mapAnnotations(term :: _), name, tparams, vparamss, tpt, rhs) :: rest
      case _ =>
        abort("The @Caching annotation can only be applied to `def` elements.")
    }
    q"..$outputs"
  }

  private def typeOf(tree: Tree): Type =
    c.typecheck(tree.duplicate.asInstanceOf[Tree], mode = c.TYPEmode, silent = true).tpe
}
