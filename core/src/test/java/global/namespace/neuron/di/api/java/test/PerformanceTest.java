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
package global.namespace.neuron.di.api.java.test;

import global.namespace.neuron.di.api.java.Incubator;
import global.namespace.neuron.di.sample.Formatter;
import global.namespace.neuron.di.sample.Greeting;
import global.namespace.neuron.di.sample.RealFormatter;
import org.hamcrest.Matcher;

import java.util.Locale;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PerformanceTest {

    private static final int LOOP_TIMES = 10;
    private static final int CONSTRUCTION_TIMES = 1_000_000;
    private static final int INVOCATION_TIMES = 1_000_000;

    private static final Formatter helloFormatter = Incubator
            .stub(RealFormatter.class)
            .bind(RealFormatter::getFormat).to("Hello %s!")
            .breed();

    public static void main(String[] args) {
        for (int i = 0; i < LOOP_TIMES; ) {
            printf("Iteration %d of %d:\n", ++i, LOOP_TIMES);

            long simple, neuron;

            simple = timeConstructions(new SimpleGreetingFactory());
            neuron = timeConstructions(new NeuronGreetingFactory());
            printf("Construction overhead [factor ]: %5.1f%n", ((double) neuron) / simple);

            simple = timeInvocations(new SimpleGreetingFactory().greeting());
            neuron = timeInvocations(new NeuronGreetingFactory().greeting());
            printf("Invocation overhead   [percent]: %5.1f%%%n", (((double) neuron) / simple - 1d) * 100d);
        }
    }

    private static void printf(String format, Object... args) {
        System.out.printf(Locale.ENGLISH, format, args);
    }

    private static long timeConstructions(GreetingFactory factory) {
        return time(CONSTRUCTION_TIMES,
                factory::greeting,
                is(instanceOf(Greeting.class)));
    }

    private static long timeInvocations(Greeting greeting) {
        return time(INVOCATION_TIMES,
                greeting::message,
                is("Hello Christian!"));
    }

    private static <T> long time(final int times,
                                 final Supplier<T> supplier,
                                 final Matcher<T> matcher) {
        final long start = System.nanoTime();
        for (int i = 0; i < times; i++) {
            assertThat(supplier.get(), matcher);
        }
        return System.nanoTime() - start;
    }

    private interface GreetingFactory {

        Greeting greeting();
    }

    private static class NeuronGreetingFactory implements GreetingFactory {

        public Greeting greeting() {
            return Incubator
                    .stub(Greeting.class)
                    .bind(Greeting::formatter).to(helloFormatter)
                    .breed();
        }
    }

    private static class SimpleGreetingFactory implements GreetingFactory {

        @Override
        public Greeting greeting() { return new SimpleGreeting(); }
    }

    private static class SimpleGreeting implements Greeting {

        @Override
        public Formatter formatter() { return helloFormatter; }
    }
}
