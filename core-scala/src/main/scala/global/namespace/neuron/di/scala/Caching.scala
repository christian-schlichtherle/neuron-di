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
class Caching(value: CachingStrategy = CachingStrategy.THREAD_SAFE) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Caching.transform
}

private object Caching {

  def transform(x: blackbox.Context)(annottees: x.Tree*): x.Tree = {
    new {
      override val c: x.type = x
    } with CachingAnnotation apply annottees.toList
  }
}
