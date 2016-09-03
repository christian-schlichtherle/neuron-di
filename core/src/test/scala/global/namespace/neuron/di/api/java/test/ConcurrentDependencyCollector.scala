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
package global.namespace.neuron.di.api.java.test

import java.util.concurrent.{CyclicBarrier, TimeUnit}

import global.namespace.neuron.di.api.java.test.ParallelSugar._
import global.namespace.neuron.di.sample.HasDependency

private class ConcurrentDependencyCollector {

  def dependenciesOf[T](neuron: HasDependency[T]): Set[T] = {
    val barrier: CyclicBarrier = new CyclicBarrier(numThreads)
    Range(0, numThreads).toSet.parallel(numThreads).flatMap { _ =>
      Range(0, numDependenciesPerThread).toSet.map { _: Int =>
        await(barrier)
        neuron.dependency
      }
    }.seq
  }

  private def await(barrier: CyclicBarrier) {
    try {
      barrier.await(1L, TimeUnit.SECONDS)
    } catch {
      case e: Any => throw new AssertionError(e)
    }
  }

  private def join(thread: Thread) {
    try {
      thread.join(1000L)
    } catch {
      case e: InterruptedException => throw new AssertionError(e)
    }
  }

  def numDependencies: Int = numDependenciesPerThread * numThreads

  def numDependenciesPerThread: Int = 10

  def numThreads: Int = 10 * Runtime.getRuntime.availableProcessors
}
