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
package global.namespace.neuron.di.java

import org.scalatest.WordSpec
import org.scalatest.Matchers._

class CachingStrategySpec extends WordSpec {

  "A caching strategy" should {
    "be enabled if and only if it's not the DISABLED caching strategy" in {
      for (strategy <- CachingStrategy.values) {
        strategy.isEnabled shouldBe (strategy != CachingStrategy.DISABLED)
      }
    }

    "have a name which can be looked up" in {
      for (strategy <- CachingStrategy.values) {
        CachingStrategy.valueOf(strategy.name) should be theSameInstanceAs strategy
      }
    }
  }
}
