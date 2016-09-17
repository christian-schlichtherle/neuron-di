/*
 * Copyright © 2016 Schlichtherle IT Services
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
package global.namespace.neuron.di.scala.test

import java.util.Date

import org.scalatest.Matchers._
import org.scalatest.WordSpec

class FixedClockModuleSpec extends WordSpec {

  "Make a fixed clock" in {
    val module = new FixedClockModule
    val clock = module.clock
    module.clock should be theSameInstanceAs clock
    clock.now should not be theSameInstanceAs(clock.now)
    new Date(0) shouldBe clock.now
  }
}
