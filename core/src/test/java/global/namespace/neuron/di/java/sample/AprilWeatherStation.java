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
package global.namespace.neuron.di.java.sample;

import global.namespace.neuron.di.java.Incubator;
import global.namespace.neuron.di.java.Neuron;

import java.util.concurrent.ThreadLocalRandom;

@Neuron
public abstract class AprilWeatherStation implements WeatherStation {

    public Temperature temperature() { return Incubator.wire(Temperature.class).using(this); }

    private static double value() { return ThreadLocalRandom.current().nextDouble(5D, 25D); }

    private static final String unit = "˚ Celsius";
}
