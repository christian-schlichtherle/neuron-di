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

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("Please add the Macro Paradise plugin to the Scala compiler to enable this macro annotation. See https://docs.scala-lang.org/overviews/macros/paradise.html .")
class Neuron(cachingStrategy: CachingStrategy = CachingStrategy.DISABLED) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Neuron.transform
}

private object Neuron {

  def transform(x: blackbox.Context)(annottees: x.Tree*): x.Tree = {
    new NeuronAnnotation {
      override val c: x.type = x
    } apply annottees.toList
  }

  def make[A <: AnyRef : c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val targetType = weakTypeOf[A]
    val targetTypeSymbol = targetType.typeSymbol

    val isNeuronType = {
      targetType.baseClasses exists { symbol =>
        (symbol == targetTypeSymbol || !symbol.asClass.isTrait) &&
          (symbol.annotations exists (a => isNeuronAnnotation(c)(a.tree)))
      }
    }

    final class MethodInfo(symbol: MethodSymbol) {

      lazy val isStable: Boolean = symbol.isStable

      lazy val name: TermName = symbol.name

      lazy val returnType: Type = symbol.returnType.asSeenFrom(targetType, symbol.owner)

      lazy val functionType: Type = c.typecheck(tq"$targetType => $returnType", mode = c.TYPEmode).tpe

      override lazy val toString: String = {
        val typePrefix = if (isNeuronType) "neuron" else "non-neuron"
        s"synapse method `$name: $returnType` as seen from $typePrefix $targetTypeSymbol"
      }
    }

    abstract class BaseBinding(info: MethodInfo) {

      import info._

      def bind: Tree = {
        {
          //noinspection ConvertibleToMethodValue
          typeCheckDependencyAs(returnType) map (returnValueBinding(_))
        } orElse {
          //noinspection ConvertibleToMethodValue
          typeCheckDependencyAs(functionType) map (functionBinding(_))
        } getOrElse {
          typeCheckDependencyAs(WildcardType) map { dependency =>
            abort(s"Dependency `$dependency` must be assignable to type `$returnType` or `$functionType`, but has type `${dependency.tpe}`:")
          } getOrElse {
            abort(s"No dependency available to bind $info:")
          }
        }
      }

      private def typeCheckDependencyAs(dependencyType: Type): Option[c.Tree] = {
        c.typecheck(dependency, pt = dependencyType, silent = true) match {
          case `EmptyTree` => None
          case typeCheckedDependency => Some(typeCheckedDependency)
        }
      }

      protected def returnValueBinding(dependency: Tree): Tree

      protected def functionBinding(dependency: Tree): Tree

      private def abort(msg: String) = c.abort(c.enclosingPosition, msg)

      private lazy val dependency: Tree = q"$name"
    }

    final class SynapseBinding(info: MethodInfo) extends BaseBinding(info) {

      import info._

      def returnValueBinding(dependency: Tree): Tree = {
        q"bind(_.$name).to($dependency)"
      }

      def functionBinding(dependency: Tree): Tree = returnValueBinding(dependency)
    }

    final class MethodBinding(info: MethodInfo) extends BaseBinding(info) {

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

    val methodInfos: Iterable[MethodInfo] = {

      def ifAbstractMethod: PartialFunction[Any, MethodSymbol] = {
        case member: Symbol if member.isAbstract && member.isMethod => member.asMethod
      }

      def isParameterless(method: MethodSymbol) = method.paramLists.flatten.isEmpty

      targetType.members collect ifAbstractMethod filter isParameterless map (new MethodInfo(_))
    }

    if (isNeuronType) {
      var tree = q"_root_.global.namespace.neuron.di.scala.Incubator.wire[$targetType]"
      tree = methodInfos.map(new SynapseBinding(_).bind).foldLeft(tree) {
        case (prefix, q"bind($methodRef).to($dependency)") => q"$prefix.bind($methodRef).to($dependency)"
      }
      q"$tree.breed"
    } else {
      val body = methodInfos.map(new MethodBinding(_).bind)
      // Splicing an empty body would remove the entire body, which would result in an error message because you can't
      // "new" an abstract type:
      if (body.nonEmpty) {
        q"new $targetType { ..$body }"
      } else {
        q"new $targetType {}"
      }
    }
  }
}
