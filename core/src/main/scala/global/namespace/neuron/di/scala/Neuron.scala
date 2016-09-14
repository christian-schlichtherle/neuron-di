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

import global.namespace.neuron.di.java.{Neuron => jNeuron}

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Please add the Macro Paradise plugin to the Scala compiler to expand this macro annotation.")
class Neuron extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Neuron.impl
}

private object Neuron {

  def impl(c: whitebox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._
    import Flag._

    implicit class FlagOps(left: FlagSet) {

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

    val inputs = annottees.toList
    val outputs: List[Tree] = inputs match {
      case ClassDef(mods @ Modifiers(flags, privateWithin, annotations), typeName @ TypeName(name), tparams, impl) :: rest =>
        val neuron = Apply(Select(New(Select(Select(Select(Select(Select(Select(Ident(termNames.ROOTPKG), TermName("global")), TermName("namespace")), TermName("neuron")), TermName("di")), TermName("java")), TypeName("Neuron"))), termNames.CONSTRUCTOR), Nil)
        ClassDef(mods.mapAnnotations(neuron :: _), typeName, tparams, impl) :: {
          if (mods.hasFlag(TRAIT) && !mods.hasFlag(INTERFACE)) {
            val implDef = ClassDef(Modifiers(flags &~ (TRAIT | DEFAULTPARAM) | ABSTRACT | SYNTHETIC, privateWithin, neuron :: annotations), TypeName("$shim"), Nil, Template(List(Ident(typeName)), noSelfType, List(DefDef(Modifiers(), termNames.CONSTRUCTOR, Nil, List(Nil), TypeTree(), Block(List(pendingSuperCall), Literal(Constant(())))))))
            rest match {
              case ModuleDef(mods, name, Template(parents, self, body)) :: rest =>
                ModuleDef(mods, name, Template(parents, self, implDef :: body)) :: rest
              case rest =>
                ModuleDef(Modifiers(flags &~ (ABSTRACT | TRAIT | DEFAULTPARAM) | SYNTHETIC, privateWithin, Nil), TermName(name), Template(Nil, noSelfType, List(implDef, DefDef(Modifiers(), termNames.CONSTRUCTOR, List(), List(List()), TypeTree(), Block(List(pendingSuperCall), Literal(Constant(()))))))) :: rest
            }
          } else {
            rest
          }
        }
      case _ =>
        inputs
    }
    q"..$outputs"
  }
}
