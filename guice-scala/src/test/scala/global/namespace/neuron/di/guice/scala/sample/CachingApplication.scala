package global.namespace.neuron.di.guice.scala.sample

import global.namespace.neuron.di.scala._

@ThreadSafeCachingNeuron
trait CachingApplication {

  def threadSafeCachedObject: AnyRef

  @Caching(CachingStrategy.THREAD_LOCAL)
  def threadLocalCachedObject: AnyRef = new AnyRef
}
