package global.tranquillity.neuron.di.api

import reflect.{classTag, ClassTag}

package object scala {

  def runtimeClassOf[A](implicit ct: ClassTag[A]): Class[A] = {
    require(ct != classTag[Nothing], "Missing type parameter.")
    ct.runtimeClass.asInstanceOf[Class[A]]
  }
}
