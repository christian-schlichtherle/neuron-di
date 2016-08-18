package global.tranquillity.neuron.di.guice.it

import javax.inject.{Inject, Named, Singleton}

import com.google.inject.{AbstractModule, Guice, Injector}
import global.tranquillity.neuron.di.guice.GuiceContext
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import com.google.inject.name.Names.named
import GuiceContextIT._

@RunWith(classOf[JUnitRunner])
class GuiceContextIT extends WordSpec {

  "A Guice context" should {
    "provide a nice DSL for building an injector" in {
      testInjector(
        new GuiceContext()
          .injector
            .module
              .bindConstant.annotatedWith(named("one")).to(1).end
              .bind(classOf[Foo])
                .annotatedWith(named("foo"))
                .to(classOf[FooImpl])
                .in(classOf[Singleton])
              .end
              .bind(classOf[Bar]).to(classOf[BarImpl]).end
            .end
          .build
      )
    }

    "build an injector with the same behavior as if using the original API" in {
      testInjector(
        Guice.createInjector(new AbstractModule {
          def configure() {
            bindConstant.annotatedWith(named("one")).to(1)
            bind(classOf[Foo])
              .annotatedWith(named("foo"))
              .to(classOf[FooImpl])
              .in(classOf[Singleton])
            bind(classOf[Bar]).to(classOf[BarImpl])
          }
        })
      )
    }
  }

  private def testInjector(injector: Injector) {
    injector getInstance classOf[Injector] should be theSameInstanceAs injector
    val bar1 = injector getInstance classOf[Bar]
    val bar2 = injector getInstance classOf[Bar]
    bar1 should not be theSameInstanceAs(bar2)
    bar1 shouldBe a[BarImpl]
    bar2 shouldBe a[BarImpl]
    bar1.foo shouldBe a[FooImpl]
    bar1.foo should be theSameInstanceAs bar2.foo
    bar1.foo.i shouldBe 1
  }
}

object GuiceContextIT {

  private trait Foo { def i: Int }

  private class FooImpl @Inject() (@Named("one") val i: Int) extends Foo

  private trait Bar { def foo: Foo }

  private class BarImpl @Inject() (@Named("foo") val foo: Foo) extends Bar
}
