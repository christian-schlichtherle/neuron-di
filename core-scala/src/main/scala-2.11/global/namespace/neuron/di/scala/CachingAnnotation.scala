/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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

// The error checks in this class must match the error chacks in
// `global.namespace.neuron.di.internal.CachingProcessor`!
private trait CachingAnnotation extends MacroAnnotation {

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree = {
    val outputs = inputs match {
      case DefDef(mods, name, tparams, vparamss, tpt, rhs) :: rest =>
        val owner = c.internal.enclosingOwner
        if (!owner.annotations.exists(a => isNeuronAnnotation(a.tree))) {
          error("A caching method must be a member of a neuron type...")
          error("... but there is no @Neuron annotation here.")(owner.pos)
        }
        if (mods hasFlag MACRO) {
          error("A caching annotation cannot be applied to a macro.")
        }
        if (mods hasFlag FINAL) {
          error("A caching method cannot be final.")
        }
        if (mods hasFlag PRIVATE) {
          error("A caching method cannot be private.")
        }
        vparamss match {
          case Nil | List(Nil) =>
          case _ =>
            error("A caching method cannot have parameters.")
        }
        c.typecheck(rhs, mode = c.TYPEmode, silent = true).tpe match {
          case TypeRef(_, sym, _)
            if sym == c.symbolOf[Unit] || sym == c.symbolOf[Nothing] =>
            error("A caching method must have a return value.")(tpt.pos)
          case ConstantType(_) =>
            error("A caching method cannot have a constant return type.")(tpt.pos)
          case _ =>
        }
        val caching = {
          val Apply(_, args) = c.prefix.tree
          val Apply(fun, _) = newCachingAnnotationTerm
          Apply(fun, args map {
            case AssignOrNamedArg(lhs@q"value", rhs: Tree) => AssignOrNamedArg(lhs, scala2javaCachingStrategy(rhs))
            case tree => scala2javaCachingStrategy(tree)
          })
        }
        DefDef(mods.mapAnnotations(caching :: _), name, tparams, vparamss, tpt, rhs) :: rest
      case other => other
    }
    q"..$outputs"
  }
}
