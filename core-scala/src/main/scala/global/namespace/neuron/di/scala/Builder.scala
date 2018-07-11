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

import java.lang.reflect.Method
import java.util.Optional

import global.namespace.neuron.di.internal.scala.runtimeClassOf
import global.namespace.neuron.di.java.{DependencyProvider, Builder => jBuilder, MethodBinding => jMethodBinding}

import scala.languageFeature.implicitConversions
import scala.reflect.ClassTag

/** @author Christian Schlichtherle */
object Builder {

  def breed[A >: Null : ClassTag](binding: MethodBinding): A = {
    jBuilder.build(runtimeClassOf[A], new jMethodBinding {

      def apply(method: Method): Optional[DependencyProvider[_]] = Optional.ofNullable(binding.applyOrElse(method, null))
    })
  }

  private implicit class DependencyProviderAdapter[A](supplier: () => A) extends DependencyProvider[A] {

    def get(): A = supplier()
  }
}
