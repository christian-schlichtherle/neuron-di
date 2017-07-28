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
import scala.reflect.macros.blackbox

@compileTimeOnly("Please add the Macro Paradise plugin to the Scala compiler to enable this macro annotation. See https://docs.scala-lang.org/overviews/macros/paradise.html .")
class Neuron(cachingStrategy: CachingStrategy = CachingStrategy.DISABLED) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Neuron.transform
}

private object Neuron {

  def transform(x: blackbox.Context)(annottees: x.Tree*): x.Tree = {
    new { val c: x.type = x } with NeuronAnnotation apply annottees.toList
  }

  def wire[A >: Null : c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val targetType = weakTypeOf[A]
    val targetTypeSymbol = targetType.typeSymbol

    val isNeuronType = {

      def isTargetTypeOrSuperClassWithNeuronAnnotation(symbol: Symbol) = {
        (symbol == targetTypeSymbol || !symbol.asClass.isTrait) && hasNeuronAnnotation(symbol)
      }

      def hasNeuronAnnotation(symbol: Symbol) = symbol.annotations exists isNeuronAnnotation

      def isNeuronAnnotation(annotation: Annotation) = annotation.tree.tpe == typeOf[jNeuron]

      targetType.baseClasses exists isTargetTypeOrSuperClassWithNeuronAnnotation
    }

    class SynapseInfo(symbol: MethodSymbol) {

      lazy val isStable: Boolean = symbol.isStable

      lazy val name: TermName = symbol.name

      lazy val returnType: Type = symbol.returnType.asSeenFrom(targetType, symbol.owner)

      lazy val functionType: Type = c.typecheck(tree = tq"$targetType => $returnType", mode = c.TYPEmode).tpe

      override def toString: String = {
        val prefix = if (isNeuronType) "neuron" else "non-neuron"
        s"synapse method `$name: $returnType` as seen from $prefix `$targetTypeSymbol`"
      }
    }

    abstract class SynapseBinder(info: SynapseInfo) {

      import info._

      def bind: Tree = {
        {
          //noinspection ConvertibleToMethodValue
          typecheckDependencyAs(returnType) map (returnValueBinding(_))
        } orElse {
          //noinspection ConvertibleToMethodValue
          typecheckDependencyAs(functionType) map (functionBinding(_))
        } getOrElse {
          typecheckDependencyAs(WildcardType) map { dependency =>
            abort(s"Dependency `$dependency` must be assignable to type `$returnType` or `$functionType`, but has type `${dependency.tpe}`:")
          } getOrElse {
            abort(s"No dependency available to bind $info:")
          }
        }
      }

      private def typecheckDependencyAs(dependencyType: Type): Option[c.Tree] = {
        c.typecheck(tree = dependency, pt = dependencyType, silent = true) match {
          case `EmptyTree` => None
          case typecheckedDependency => Some(typecheckedDependency)
        }
      }

      protected def returnValueBinding(dependency: Tree): Tree

      protected def functionBinding(dependency: Tree): Tree

      private def abort(msg: String) = c.abort(c.enclosingPosition, msg)

      private lazy val dependency: Tree = q"$name"
    }

    class NeuronSynapseBinder(info: SynapseInfo) extends SynapseBinder(info) {

      import info._

      def returnValueBinding(dependency: Tree): Tree = {
        q"bind(_.$name).to($dependency)"
      }

      def functionBinding(dependency: Tree): Tree = returnValueBinding(dependency)
    }

    class NonNeuronSynapseBinder(info: SynapseInfo) extends SynapseBinder(info) {

      import info._

      def returnValueBinding(dependency: Tree): Tree = {
        if (isStable) {
          q"lazy val $name: $returnType = $dependency"
        } else {
          q"def $name: $returnType = $dependency"
        }
      }

      def functionBinding(dependency: Tree): Tree = {
        val fun = c.untypecheck(dependency) // required for Scala 2.12.[0,1], but not 2.11.[0,8]!
        if (isStable) {
          q"lazy val $name: $returnType = $fun(this)"
        } else {
          q"def $name: $returnType = $fun(this)"
        }
      }
    }

    val synapseInfos = {

      def ifAbstractMethod: PartialFunction[Any, MethodSymbol] = {
        case member: Symbol if member.isAbstract && member.isMethod => member.asMethod
      }

      def isParameterless(method: MethodSymbol) = method.paramLists.flatten.isEmpty

      targetType.members collect ifAbstractMethod filter isParameterless map (new SynapseInfo(_))
    }

    if (isNeuronType) {
      var tree = q"_root_.global.namespace.neuron.di.scala.Incubator.wire[$targetType]"
      tree = (tree /: (synapseInfos map (new NeuronSynapseBinder(_).bind))) {
        case (prefix, q"bind($synapseRef).to($dependency)") => q"$prefix.bind($synapseRef).to($dependency)"
      }
      q"$tree.breed"
    } else {
      q"new $targetType { ..${synapseInfos map (new NonNeuronSynapseBinder(_).bind)} }"
    }
  }
}
