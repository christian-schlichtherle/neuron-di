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

  "A guice context" should {
    "provide a nice DSL" in {
      testInjector(
        new GuiceContext()
          .injector
            .module
              .bindConstant.annotatedWith(named("i")).to(1).end
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

    "yield the same results as the original Guice API" in {
      testInjector(
        Guice.createInjector(new AbstractModule {
          def configure() {
            bindConstant.annotatedWith(named("i")).to(1)
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
    val bar1 = injector.getInstance(classOf[Bar])
    val bar2 = injector.getInstance(classOf[Bar])
    bar1 should not be theSameInstanceAs(bar2)
    bar1 should be(a[BarImpl])
    bar2 should be(a[BarImpl])
    bar1.foo should be(a[FooImpl])
    bar1.foo should be theSameInstanceAs bar2.foo
    bar1.foo.i should be(1)
  }
}

object GuiceContextIT {

  private trait Foo { def i: Int }

  private class FooImpl @Inject() (@Named("i") val i: Int) extends Foo

  private trait Bar { def foo: Foo }

  private class BarImpl @Inject() (@Named("foo") val foo: Foo) extends Bar
}
