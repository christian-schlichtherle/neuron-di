package global.tranquillity.neuron.di.guice.it

import javax.inject.{Inject, Named, Singleton}

import com.google.inject._
import com.google.inject.name.Names.named
import global.tranquillity.neuron.di.guice.ModuleSugar
import global.tranquillity.neuron.di.guice.it.ModuleSugarSpec._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class ModuleSugarSpec extends WordSpec {

  "Module sugar" should {
    "provide a nice DSL for configuring a module" in {
      testInjector(
        Guice.createInjector(new ModuleSugar {

          def configure() {
            bindConstant.named("one").to(1)
            bindClass[Foo]
              .named("foo")
              .toClass[FooImpl]
              .inScope[Singleton]
            bindClass[Bar].toClass[BarImpl]
          }
        })
      )
    }

    "configure a module with the same behavior as if using the original API" in {
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

object ModuleSugarSpec {

  private trait Foo { def i: Int }

  private class FooImpl @Inject() (@Named("one") val i: Int) extends Foo

  private trait Bar { def foo: Foo }

  private class BarImpl @Inject() (@Named("foo") val foo: Foo) extends Bar
}
