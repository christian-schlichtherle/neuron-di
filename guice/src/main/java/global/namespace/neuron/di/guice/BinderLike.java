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
package global.namespace.neuron.di.guice;

import com.google.inject.Binder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.binder.ConstantBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.api.Neuron;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.inject.name.Names.named;

@Neuron
public interface BinderLike {

    Binder binder();

    default ConstantBindingBuilder bindConstantNamed(String name) {
        return binder().bindConstant().annotatedWith(named(name));
    }

    default <T> ScopedBindingBuilder bindNeuron(Class<T> runtimeClass) {
        return binder().bind(runtimeClass).toProvider(neuronProvider(runtimeClass));
    }

    @SuppressWarnings("unchecked")
    default <T> Provider<T> neuronProvider(final Class<T> runtimeClass) {
        final Provider<Injector> injectorProvider =
                binder().getProvider(Injector.class);
        return Incubator
                .stub(NeuronProvider.class)
                .bind(NeuronProvider::injector).to(injectorProvider::get)
                .bind(NeuronProvider::runtimeClass).to(runtimeClass)
                .breed();
    }
}
