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
package global.namespace.neuron.di.api.test

import scala.collection.parallel.{ForkJoinTaskSupport, ParIterableLike}
import scala.collection.{Parallel, Parallelizable}
import scala.concurrent.forkjoin.ForkJoinPool

trait ParallelSugar {

  type HasPar[ParRepr <: Parallel] = Parallelizable[_, ParRepr]

  implicit class WithHasPar[T <: HasTasksupport](coll: HasPar[T]) {

    def parallel(parallelism: Int): T = coll.par.parallelism(parallelism)
  }

  type HasTasksupport = ParIterableLike[_, _, _]

  implicit class WithHasTaskSupport[T <: HasTasksupport](par: T) {

    def parallelism(parallelism: Int): T = {
      par.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(parallelism))
      par
    }
  }
}

object ParallelSugar extends ParallelSugar
