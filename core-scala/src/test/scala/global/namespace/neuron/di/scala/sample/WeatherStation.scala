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
package global.namespace.neuron.di.scala.sample

import java.util.concurrent.ThreadLocalRandom

import global.namespace.neuron.di.scala._

@Neuron
trait WeatherStation extends Clock {

  def temperature: Temperature

  @Neuron
  trait Temperature {

    val value: Double

    val unit: String
  }
}

@Neuron
trait AprilWeatherStation extends WeatherStation {

  def temperature: Temperature = Incubator
    .stub[Temperature]
    .bind(_.value).to(temperatureValue)
    .bind(_.unit).to("˚ Celsius")
    .breed

  private def temperatureValue: Double =
    ThreadLocalRandom.current.nextDouble(5D, 25D)
}