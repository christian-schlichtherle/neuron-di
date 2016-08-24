package global.tranquillity.neuron.di.core

import reflect.ClassTag
import reflect.classTag

package object scala {

  def runtimeClassOf[A](implicit ct: ClassTag[A]) = {
    require(ct != classTag[Nothing], "Missing type parameter.")
    ct.runtimeClass.asInstanceOf[Class[A]]
  }
}
