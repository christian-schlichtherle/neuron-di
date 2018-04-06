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

/** @author Christian Schlichtherle */
private trait CachingAnnotation extends MacroAnnotation {

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree = {
    val outputs = inputs match {
      case DefDef(mods, name, tparams, vparamss, tpt, rhs) :: rest =>
        val owner = c.internal.enclosingOwner
        if (!owner.annotations.exists(a => isNeuronAnnotation(a.tree))) {
          error("A caching annotation can only be applied to method definitions in a neuron type...")
          error("... but there is no @Neuron annotation here.")(owner.pos)
        }
        if (mods hasFlag MACRO) {
          abort("A caching annotation cannot be applied to a macro definition.")
        }
        if (mods hasFlag FINAL) {
          error("A caching annotation cannot be applied to a final method definition.")
        }
        if (mods hasFlag PRIVATE) {
          error("A caching annotation cannot be applied to a private method definition.")
        }
        vparamss match {
          case Nil | List(Nil) =>
          case _ =>
            abort("A caching annotation cannot be applied to a method definition with parameters.")
        }
        typeOf(rhs) match {
          case ConstantType(_) =>
            error("A caching annotation cannot be applied to a method definition with a constant return type.")(tpt.pos)
          case TypeRef(_, sym, _)
            if sym == c.symbolOf[Unit] || sym == c.symbolOf[Nothing] =>
            error("A caching annotation cannot be applied to a method definition with return type `Unit` or `Nothing`.")(tpt.pos)
          case _ =>
        }
        val caching = {
          val Apply(_, args) = c.prefix.tree
          val Apply(fun, _) = newCachingAnnotationTerm
          Apply(fun, args map {
            case q"value = ${rhs: Tree}" =>
              q"value = ${scala2javaCachingStrategy(rhs)}" match {
                case Assign(x, y) => AssignOrNamedArg(x, y)
                case other => other
              }
            case tree =>
              scala2javaCachingStrategy(tree)
          })
        }
        DefDef(mods.mapAnnotations(caching :: _), name, tparams, vparamss, tpt, rhs) :: rest
      case _ =>
        abort("The @Caching annotation can only be applied to `def` elements.")
    }
    q"..$outputs"
  }

  private def typeOf(tree: Tree): Type =
    c.typecheck(tree.duplicate.asInstanceOf[Tree], mode = c.TYPEmode, silent = true).tpe
}
