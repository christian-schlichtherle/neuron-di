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
package global.namespace.neuron.di.internal;

import net.sf.cglib.proxy.Callback;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

final class CGContext {

    private final NeuronElement element;
    private final Function<Method, Supplier<?>> binding;

    private Callback[] callbacks;

    CGContext(final NeuronElement element,
              final Function<Method, Supplier<?>> binding) {
        this.element = element;
        this.binding = binding;
    }

    Class<?> runtimeClass() { return element.runtimeClass(); }

    MethodElement element(Method method) { return element.element(method); }

    Supplier<?> supplier(Method method) { return binding.apply(method); }

    Callback[] callbacks(Supplier<Callback[]> supplier) {
        final Callback[] callbacks = this.callbacks;
        return null != callbacks ? callbacks : (this.callbacks = supplier.get());
    }
}
