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

import scala.annotation.tailrec

// The error checks in this class must match the error checks in
// `global.namespace.neuron.di.internal.NeuronProcessor`!
private trait NeuronAnnotation extends MacroAnnotation {

  import c.universe._
  import Flag._

  def apply(inputs: List[Tree]): Tree = {
    val outputs = inputs match {
      case ClassDef(mods@Modifiers(flags, privateWithin, annotations), tname@TypeName(name), tparams, impl) :: rest =>
        if (!hasStaticContext) {
          error("A neuron type must have a static context.")
        }
        if (mods hasFlag FINAL) {
          error("A neuron class cannot be final.")
        }
        if (!(mods hasFlag ABSTRACT)) {
          warn("A neuron class should be abstract.")
        }
        if (!hasEitherNoConstructorOrANonPrivateConstructorWithoutParameters(impl)) {
          error("A neuron type must have either no constructor or a non-private constructor without parameters.")
        }
        if ((mods hasFlag INTERFACE) && !isCachingDisabled) {
          warn("A neuron interface should not have a caching strategy.")
        }
        if (isSerializable(impl)) {
          warn("A neuron type should not be serializable.")
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
              case NamedArg(lhs@q"cachingStrategy", rhs: Tree) => NamedArg(lhs, scala2javaCachingStrategy(rhs))
              case tree => tree
            })
          }
          ClassDef(mods.mapAnnotations(shim ::: neuron :: _), tname, tparams, applyCachingAnnotation(impl)) :: {
            if (needsShim) {
              val shimMods = Modifiers(flags &~ (DEFAULTPARAM | TRAIT) | ABSTRACT | SYNTHETIC, privateWithin, neuron :: annotations)
              val shimDef = q"$shimMods class $$shim[..$tparams] extends $tname[..${tparams.map(_.name)}]"
              rest match {
                case ModuleDef(moduleMods, moduleName, Template(parents, self, body)) :: moduleRest =>
                  ModuleDef(moduleMods, moduleName, Template(parents, self, shimDef :: body)) :: moduleRest
                case _ =>
                  // This should be SYNTHETIC, however this would break SBT 1.1.2, 1.1.4, 1.2.7 et al.
                  val moduleMods = Modifiers(flags &~ (ABSTRACT | TRAIT | DEFAULTPARAM), privateWithin, annotations)
                  q"$moduleMods object ${TermName(name)} { $shimDef }" :: rest
              }
            } else {
              rest
            }
          }
        }
      case other => other
    }
    q"..$outputs"
  }

  private def hasStaticContext = enclosingOwner.isStatic

  private lazy val enclosingOwner = c.internal.enclosingOwner

  private def hasEitherNoConstructorOrANonPrivateConstructorWithoutParameters(template: Template) = {
    val Template(_, _, body) = template
    val constructors = body collect { case c@DefDef(_, termNames.CONSTRUCTOR, _, _, _, _) => c }
    constructors.isEmpty || (constructors exists isNonPrivateConstructorWithoutParameters)
  }

  private def isNonPrivateConstructorWithoutParameters(tree: Tree) = {
    tree match {
      case DefDef(mods, termNames.CONSTRUCTOR, _, Nil | List(Nil), _, _)
        if !mods.hasFlag(PRIVATE) => true
      case _ => false
    }
  }

  private def isCachingDisabled = {
    !c.prefix.tree.collect {
      case q"cachingStrategy = ${Select(_, TermName(name: String))}" => name
      case q"cachingStrategy = ${Ident(TermName(name: String))}" => name
    }.exists(_ != "DISABLED")
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
      case ValDef(mods@Modifiers(_, _, annotations), name, tpt, EmptyTree)
        if !annotations.exists(isCachingAnnotation) && !mods.hasFlag(PRIVATE) =>
        ValDef(mods.mapAnnotations(newCachingAnnotationTerm :: _), name, tpt, EmptyTree)
      case other =>
        other
    })
  }
}
