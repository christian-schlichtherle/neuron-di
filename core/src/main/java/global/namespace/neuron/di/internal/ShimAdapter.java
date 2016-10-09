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

import java.util.Optional;
import java.util.function.Function;

final class ShimAdapter<N, V> implements Function<Class<N>, V> {

    private final Function<Class<? extends N>, V> function;

    ShimAdapter(final Function<Class<? extends N>, V> function) { this.function = function; }

    @SuppressWarnings("unchecked")
    @Override
    public V apply(Class<N> neuronClass) {
        return function.apply(Optional.ofNullable(neuronClass.getDeclaredAnnotation(Shim.class))
                .<Class<? extends N>>map(annotation -> (Class<? extends N>) annotation.value())
                .orElse(neuronClass));
    }
}
