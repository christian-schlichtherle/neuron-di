package global.namespace.neuron.di.api

import reflect.{ClassTag, classTag}

package object scala {

  def runtimeClassOf[A](implicit ct: ClassTag[A]): Class[A] = {
    require(ct != classTag[Nothing], "Missing type parameter.")
    ct.runtimeClass.asInstanceOf[Class[A]]
  }
}
