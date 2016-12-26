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

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("You need to add the Macro Paradise plugin to the Scala compiler to use this macro annotation. See http://docs.scala-lang.org/overviews/macros/paradise .")
class Neuron(cachingStrategy: CachingStrategy = CachingStrategy.DISABLED)
  extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Neuron.transform
}

private object Neuron {

  def transform(x: blackbox.Context)(annottees: x.Tree*): x.Tree = {
    new { val c: x.type = x } with NeuronAnnotation apply annottees.toList
  }

  def wire[A : c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    def ifAbstractMethod: PartialFunction[Symbol, MethodSymbol] = {
      case member if member.isAbstract && member.isMethod => member.asMethod
    }

    def isParameterless(method: MethodSymbol) = method.paramLists.flatten.isEmpty

    def synapseMethods = weakTypeOf[A].members.collect(ifAbstractMethod).filter(isParameterless)

    def stubTerm = q"""_root_.global.namespace.neuron.di.scala.Incubator.stub[${weakTypeOf[A]}]"""

    def bindingTerm = {
      (stubTerm /: synapseMethods) { (term, synapseMethod) =>
        val synapseName = synapseMethod.name
        val synapseType = synapseMethod.returnType
        q"""$term.bind(_.$synapseName).to($synapseName: $synapseType)"""
      }
    }

    q"""$bindingTerm.breed"""
  }
}
