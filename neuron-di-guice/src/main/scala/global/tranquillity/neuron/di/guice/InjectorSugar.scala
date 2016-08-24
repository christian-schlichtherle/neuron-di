package global.tranquillity.neuron.di.guice

import com.google.inject.Injector

import scala.reflect.ClassTag
import global.tranquillity.neuron.di.core.scala._

trait InjectorSugar {

  implicit class WithInjector(injector: Injector) {

    def getInstanceOf[A](implicit ct: ClassTag[A]): A =
      injector.getInstance(runtimeClassOf(ct))
  }
}

object InjectorSugar extends InjectorSugar
