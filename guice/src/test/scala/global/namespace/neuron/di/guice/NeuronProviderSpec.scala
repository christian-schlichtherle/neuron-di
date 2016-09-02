package global.namespace.neuron.di.guice

import com.google.inject.name.Names.named
import com.google.inject.{Injector, Key, Provider}
import global.namespace.neuron.di.api.scala.Incubator
import global.namespace.neuron.di.guice.sample.{NeuronWithQualifiedSynapses, TestBindingAnnotation, TestQualifier}
import org.scalatest.WordSpec
import org.scalatest.Matchers._
import org.specs2.mock.Mockito

class NeuronProviderSpec extends WordSpec with Mockito {

  "A neuron provider" should {
    "use a given injector to create providers for keys with qualifying and binding annotations" in {
      val injector = mock[Injector]
      val provider = Incubator
        .stub[NeuronProvider[NeuronWithQualifiedSynapses]]
        .bind(_.injector()).to(injector)
        .bind(_.runtimeClass()).to(classOf[NeuronWithQualifiedSynapses])
        .breed
      val fooKey = Key.get(classOf[String], named("foo"))
      val barKey = Key.get(classOf[String], named("bar"))
      val boomKey = Key.get(classOf[String], classOf[TestQualifier])
      val bangKey = Key.get(classOf[String], classOf[TestBindingAnnotation])

      injector.getProvider(fooKey) returns mock[Provider[String]]
      injector.getProvider(barKey) returns mock[Provider[String]]
      injector.getProvider(boomKey) returns mock[Provider[String]]
      injector.getProvider(bangKey) returns mock[Provider[String]]

      provider.get shouldBe a[NeuronWithQualifiedSynapses]

      there was one(injector).getProvider(fooKey)
      there was one(injector).getProvider(barKey)
      there was one(injector).getProvider(boomKey)
      there was one(injector).getProvider(bangKey)
      there were noMoreCallsTo(injector)
    }
  }
}
