package global.tranquillity.neuron.di.guice

import com.google.inject.Injector

import scala.reflect.ClassTag

trait InjectorSugar {

  implicit class WithInjector(injector: Injector) {

    def getInstanceOf[A](implicit ct: ClassTag[A]): A =
      injector.getInstance(ct.runtimeClass).asInstanceOf[A]
  }
}

object InjectorSugar extends InjectorSugar
