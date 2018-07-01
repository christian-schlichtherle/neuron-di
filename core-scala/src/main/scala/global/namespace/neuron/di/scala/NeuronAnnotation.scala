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

import scala.annotation.tailrec

/** @author Christian Schlichtherle */
private trait NeuronAnnotation extends MacroAnnotation {

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree = {
    val outputs = inputs match {
      case ClassDef(mods @ Modifiers(flags, privateWithin, annotations), tname @ TypeName(name), tparams, impl) :: rest =>
        if (!hasStaticContext) {
          error("A neuron type must have a static context.")
        }
        if (!(mods hasFlag ABSTRACT)) {
          warning("A neuron class must be abstract.")
        }
        if (mods hasFlag FINAL) {
          error("A neuron class must not be final.")
        }
        if (!hasNonPrivateConstructorWithoutParameters(impl)) {
          error("A neuron class must have a non-private constructor without parameters.")
        }
        if (isSerializable(impl)) {
          error("A neuron class or interface must not be serializable.")
        }
        if (c.hasErrors) {
          inputs
        } else {
          val needsShim = (mods hasFlag TRAIT) && !(mods hasFlag INTERFACE)
          val shim = {
            if (needsShim) {
              // Due to https://issues.scala-lang.org/browse/SI-7551 , we have to put the binary class name into the
              // shim annotation instead of just the class literal which is bad because the naming schema is supposed to
              // be an implementation detail of the Scala compiler which may change without notice.
              val binaryName = {
                binaryNameOf(enclosingOwner) +
                  (if (enclosingOwner.isPackage) '.' else '$') +
                  tname.encodedName +
                  "$$shim"
              }
              q"new _root_.global.namespace.neuron.di.internal.Shim(name = $binaryName)" :: Nil
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
          ClassDef(mods.mapAnnotations(shim ::: neuron :: _), tname, tparams, applyCachingAnnotation(impl)) :: {
            if (needsShim) {
              val shimMods = Modifiers(flags &~ (TRAIT | DEFAULTPARAM) | ABSTRACT | SYNTHETIC, privateWithin, neuron :: annotations)
              val shimDef = q"$shimMods class $$shim extends $tname"
              rest match {
                case ModuleDef(moduleMods, moduleName, Template(parents, self, body)) :: moduleRest =>
                  ModuleDef(moduleMods, moduleName, Template(parents, self, shimDef :: body)) :: moduleRest
                case _ =>
                  // This should be SYNTHETIC, however this would break SBT 1.1.2, 1.1.4 et al.
                  val moduleMods = Modifiers(flags &~ (ABSTRACT | TRAIT | DEFAULTPARAM), privateWithin, annotations)
                  q"$moduleMods object ${TermName(name)} { $shimDef }" :: rest
              }
            } else {
              rest
            }
          }
        }
      case _ =>
        abort("A neuron annotation can only be applied to classes or traits.")
    }
    q"..$outputs"
  }

  private def hasStaticContext = enclosingOwner.isStatic

  private lazy val enclosingOwner = c.internal.enclosingOwner

  private def hasNonPrivateConstructorWithoutParameters(template: Template) = {
    val Template(_, _, body) = template
    !(body exists isConstructor) ||
      (body exists isNonPrivateConstructorWithoutParameters)
  }

  private def isConstructor(tree: Tree) = {
    tree match {
      case DefDef(_, termNames.CONSTRUCTOR, _, _, _, _) => true
      case _ => false
    }
  }

  private def isNonPrivateConstructorWithoutParameters(tree: Tree) = {
    tree match {
      case DefDef(mods, termNames.CONSTRUCTOR, _, Nil | List(Nil), _, _)
        if !mods.hasFlag(PRIVATE) => true
      case _ => false
    }
  }

  private def isSerializable(template: Template) = {
    template.parents.exists(_.toString == classOf[java.io.Serializable].getName)
  }

  private def binaryNameOf(symbol: Symbol): String = {

    @tailrec
    def binaryNameOf(symbol: Symbol, tail: String): String = {
      symbol match {
        case NoSymbol =>
          "" + tail
        case other if other.isPackage =>
          other.fullName + tail
        case other =>
          val oo = other.owner
          binaryNameOf(oo, (if (oo.isPackage) "." else "$") + other.name.encodedName + tail)
      }
    }

    binaryNameOf(symbol, "")
  }

  private def applyCachingAnnotation(template: Template) = {
    val Template(parents, self, body) = template
    Template(parents, self, body map {
      case ValDef(mods @ Modifiers(_, _, annotations), name, tpt, EmptyTree)
        if !annotations.exists(isCachingAnnotation) && !mods.hasFlag(PRIVATE) =>
        ValDef(mods.mapAnnotations(newCachingAnnotationTerm :: _), name, tpt, EmptyTree)
      case other =>
        other
    })
  }
}
