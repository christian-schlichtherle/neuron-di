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
package global.namespace.neuron.di.java.test;

import global.namespace.neuron.di.java.Incubator;
import global.namespace.neuron.di.java.sample.AprilWeatherStation;
import global.namespace.neuron.di.java.sample.WeatherStation;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AprilWeatherStationTest {

    @Test
    public void testAprilWeatherStation() {
        final WeatherStation station = Incubator.breed(AprilWeatherStation.class);
        assertThat(station.now(), is(not(sameInstance(station.now()))));
        assertThat(new Date(), is(lessThanOrEqualTo(station.now())));
        final WeatherStation.Temperature temperature = station.temperature();
        assertThat(temperature, is(not(sameInstance(station.temperature()))));
        assertThat(temperature.value(), is(temperature.value()));
        assertThat(temperature.value(), is(greaterThanOrEqualTo(5D)));
        assertThat(temperature.value(), is(lessThan(25D)));
        assertThat(temperature.unit(), is(temperature.unit()));
        assertThat(temperature.unit(), is("˚ Celsius"));
    }

    @Test
    public void testAprilWeatherStationWithoutMatchers() {
        final WeatherStation station = Incubator.breed(AprilWeatherStation.class);
        assert station.now() != station.now();
        assert new Date().compareTo(station.now()) <= 0;
        final WeatherStation.Temperature temperature = station.temperature();
        assert temperature != station.temperature();
        assert temperature.value() == temperature.value();
        assert temperature.value() >= 5D;
        assert temperature.value() < 25D;
        assert temperature.unit().equals(temperature.unit());
        assert temperature.unit().equals("˚ Celsius");
    }
}
