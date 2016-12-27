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
import scala.reflect.macros.{TypecheckException, blackbox}

@compileTimeOnly("You need to add the Macro Paradise plugin to the Scala compiler to use this macro annotation. See http://docs.scala-lang.org/overviews/macros/paradise .")
class Neuron(cachingStrategy: CachingStrategy = CachingStrategy.DISABLED)
  extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Neuron.transform
}

private object Neuron {

  def transform(x: blackbox.Context)(annottees: x.Tree*): x.Tree = {
    new { val c: x.type = x } with NeuronAnnotation apply annottees.toList
  }

  def wire[A <: AnyRef : c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val targetType = weakTypeOf[A]

    def isNeuronType = {
      val targetTypeSymbol = targetType.typeSymbol

      def isNeuronAnnotation(annotation: Annotation) = annotation.tree.tpe == typeOf[jNeuron]

      def hasNeuronAnnotation(symbol: Symbol) = symbol.annotations exists isNeuronAnnotation

      def isTargetTypeOrSuperClassWithNeuronAnnotation(symbol: Symbol) = {
        (symbol == targetTypeSymbol || !symbol.asClass.isTrait) && hasNeuronAnnotation(symbol)
      }

      targetType.baseClasses exists isTargetTypeOrSuperClassWithNeuronAnnotation
    }

    def abort(msg: String) = c.abort(c.enclosingPosition, msg)

    def bindingTerm = {

      def stubTerm = q"""_root_.global.namespace.neuron.di.scala.Incubator.stub[$targetType]"""

      def synapses = {

        def ifAbstractMethod: PartialFunction[Symbol, MethodSymbol] = {
          case member if member.isAbstract && member.isMethod => member.asMethod
        }

        def isParameterless(method: MethodSymbol) = method.paramLists.flatten.isEmpty

        targetType.members.collect(ifAbstractMethod).filter(isParameterless)
      }

      (stubTerm /: synapses) { (term, synapse) =>
        val dependencyName = synapse.name
        val dependencyType = synapse.returnType.asSeenFrom(targetType, synapse.owner)
        val tree = q"$dependencyName"
        c.typecheck(tree = tree, pt = dependencyType, silent = true) match {
          case `EmptyTree` =>
            val treeType = c.typecheck(tree).tpe
            abort(s"Typecheck failed: Dependency `$dependencyName` must be assignable to type `$dependencyType`, but has type `$treeType`.")
          case _ =>
            q"""$term.bind(_.$dependencyName).to($tree)"""
        }
      }
    }

    if (isNeuronType) {
      q"""$bindingTerm.breed"""
    } else {
      abort(s"$targetType is not a @Neuron type.")
    }
  }
}
