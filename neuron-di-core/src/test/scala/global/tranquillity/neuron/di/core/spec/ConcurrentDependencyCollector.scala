package global.tranquillity.neuron.di.core.spec

import java.util.concurrent.{CyclicBarrier, TimeUnit}

import global.tranquillity.neuron.di.core.spec.ParallelSugar._
import global.tranquillity.neuron.di.core.test.HasDependency

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



