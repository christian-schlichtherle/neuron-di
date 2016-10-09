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

private trait NeuronAnnotation extends MacroAnnotation {

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree = {
    val outputs = inputs match {
      case ClassDef(mods@Modifiers(flags, privateWithin, annotations), tpname@TypeName(name), tparams, impl) :: rest =>
        if (mods hasFlag TRAIT) {
          if (!hasStaticContext) {
            error("A neuron trait must have a static context.")
          }
        } else {
          if (!(mods hasFlag ABSTRACT)) {
            warning("A neuron class should be abstract.")
          }
          if (mods hasFlag FINAL) {
            error("A neuron class cannot be final.")
          }
          if (!hasNonPrivateConstructorWithoutParameters(impl)) {
            error("A neuron class must have a non-private constructor without parameters.")
          }
          if (!hasStaticContext) {
            error("A neuron class must have a static context.")
          }
        }
        if (c.hasErrors) {
          inputs
        } else {
          val needsShim = (mods hasFlag TRAIT) && !(mods hasFlag INTERFACE)
          val shim = {
            if (needsShim) {
              q"new _root_.global.namespace.neuron.di.internal.Shim(classOf[${TermName(name)}.$$shim])" :: Nil
            } else {
              Nil
            }
          }
          val neuron = {
            val Apply(_, args) = c.prefix.tree
            val Apply(fun, _) = newNeuronAnnotationTerm
            Apply(fun, args map {
              case q"cachingStrategy = ${rhs: Tree}" =>
                q"cachingStrategy = ${scala2javaCachingStrategy(rhs)}" match {
                  case Assign(x, y) => AssignOrNamedArg(x, y)
                  case other => other
                }
              case tree =>
                tree
            })
          }
          ClassDef(mods.mapAnnotations(neuron :: _ ::: shim), tpname, tparams, applyCachingAnnotation(impl)) :: {
            if (needsShim) {
              val shimMods = Modifiers(flags &~ (TRAIT | DEFAULTPARAM) | ABSTRACT | SYNTHETIC, privateWithin, neuron :: annotations)
              val shimDef = q"$shimMods class $$shim extends $tpname"
              rest match {
                case ModuleDef(moduleMods, moduleName, Template(parents, self, body)) :: moduleRest =>
                  ModuleDef(moduleMods, moduleName, Template(parents, self, shimDef :: body)) :: moduleRest
                case _ =>
                  val moduleMods = Modifiers(flags &~ (ABSTRACT | TRAIT | DEFAULTPARAM) | SYNTHETIC, privateWithin, annotations)
                  q"$moduleMods object ${TermName(name)} { $shimDef }" :: rest
              }
            } else {
              rest
            }
          }
        }
      case _ =>
        abort("The @Neuron annotation can only be applied to classes or traits.")
    }

    q"..$outputs"
  }

  private def hasNonPrivateConstructorWithoutParameters(template: Template): Boolean = {
    val Template(_, _, body) = template
    !(body exists isConstructor) ||
      (body exists isNonPrivateConstructorWithoutParameters)
  }

  private def isConstructor(tree: Tree): Boolean = {
    tree match {
      case DefDef(_, termNames.CONSTRUCTOR, _, _, _, _) => true
      case _ => false
    }
  }

  private def isNonPrivateConstructorWithoutParameters(tree: Tree): Boolean = {
    tree match {
      case DefDef(mods, termNames.CONSTRUCTOR, _, Nil | List(Nil), _, _)
        if !mods.hasFlag(PRIVATE) => true
      case _ => false
    }
  }

  private def hasStaticContext: Boolean = c.internal.enclosingOwner.isStatic

  private def applyCachingAnnotation(template: Template): Template = {
    val Template(parents, self, body) = template
    Template(parents, self, body map {
      case valDef@ValDef(mods@Modifiers(_, _, annotations), name, tpt, EmptyTree)
        if !annotations.exists(isCachingAnnotation) && !mods.hasFlag(PRIVATE) =>
        ValDef(mods.mapAnnotations(newCachingAnnotationTerm :: _), name, tpt, EmptyTree)
      case other =>
        other
    })
  }
}
