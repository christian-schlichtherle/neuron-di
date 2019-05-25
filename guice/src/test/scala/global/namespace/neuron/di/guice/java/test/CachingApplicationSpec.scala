/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package global.namespace.neuron.di.guice.java.test

import global.namespace.neuron.di.guice.java.sample.CachingApplication
import global.namespace.neuron.di.java.Incubator
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.util.control.NonFatal

class CachingApplicationSpec extends WordSpec {

  "A `CachingApplication` object" when {
    val app = Incubator.breed(classOf[CachingApplication])

    "calling its `threadSafeCachedObject` method" should {
      val o = app.threadSafeCachedObject

      "return the same object on the same thread" in {
        app.threadSafeCachedObject should be theSameInstanceAs o
      }

      "return the same object on a different thread" in {
        run {
          app.threadSafeCachedObject should be theSameInstanceAs o
        }
      }
    }

    "calling its `threadLocalCachedObject` method" should {
      val o = app.threadLocalCachedObject

      "return the same object on the same thread" in {
        app.threadLocalCachedObject should be theSameInstanceAs o
      }

      "return a different object on a different thread" in {
        run {
          val p = app.threadLocalCachedObject
          p shouldNot be theSameInstanceAs o
          app.threadLocalCachedObject should be theSameInstanceAs p
        }
      }
    }
  }

  private def run(f: => Any) {
    new Thread() {

      var t: Option[Throwable] = None

      override def run() {
        try {
          f
        } catch {
          case NonFatal(e) => t = Some(e)
        }
      }

      def check() {
        start()
        join()
        t foreach (throw _)
      }
    } check ()
  }
}
