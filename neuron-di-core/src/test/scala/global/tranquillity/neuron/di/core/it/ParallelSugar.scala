package global.tranquillity.neuron.di.core.it

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
