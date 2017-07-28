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
package global.namespace.neuron.di.java.test

import java.util.concurrent.{CyclicBarrier, TimeUnit}

import global.namespace.neuron.di.java.sample.HasDependency
import global.namespace.neuron.di.java.test.ParallelSugar._

private class ConcurrentDependencyCollector {

  val numThreads: Int = 10 * Runtime.getRuntime.availableProcessors

  val numInjectionsPerThread: Int = 10

  val numInjections: Int = numThreads * numInjectionsPerThread

  def dependenciesOf[T](neuron: HasDependency[T]): Set[T] = {
    val barrier: CyclicBarrier = new CyclicBarrier(numThreads)
    Range(0, numThreads).toSet.parallel(numThreads).flatMap { _ =>
      Range(0, numInjectionsPerThread).toSet.map { _: Int =>
        try {
          barrier.await(3L, TimeUnit.SECONDS)
        } catch {
          case e: Any => throw new AssertionError(e)
        }
        val dependency = neuron.get
        assert(null != dependency)
        dependency
      }
    }.seq
  }
}
