package global.tranquillity.neuron.di.guice.scala

import com.google.inject.Injector
import global.tranquillity.neuron.di.core.scala._

import scala.reflect.ClassTag

trait InjectorSugar {

  implicit class WithInjector(injector: Injector) {

    def getInstanceOf[A](implicit ct: ClassTag[A]): A =
      injector.getInstance(runtimeClassOf(ct))
  }
}

object InjectorSugar extends InjectorSugar
