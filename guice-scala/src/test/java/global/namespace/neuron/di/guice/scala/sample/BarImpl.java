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
package global.namespace.neuron.di.guice.scala.sample;

import javax.inject.Inject;
import javax.inject.Named;

public class BarImpl implements Bar {

    private final Foo foo;

    @Inject
    public BarImpl(final @Named("impl") Foo foo) { this.foo = foo; }

    @Override
    public Foo foo() { return foo; }
}
