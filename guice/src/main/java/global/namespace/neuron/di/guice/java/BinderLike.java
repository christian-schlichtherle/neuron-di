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
package global.namespace.neuron.di.guice.java;

import com.google.inject.*;
import com.google.inject.binder.ConstantBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import global.namespace.neuron.di.java.Incubator;

import static com.google.inject.name.Names.named;

public interface BinderLike {

    Binder binder();

    default ConstantBindingBuilder bindConstantNamed(String name) {
        return binder().bindConstant().annotatedWith(named(name));
    }

    default <T> ScopedBindingBuilder bindNeuron(Class<T> type) { return bindNeuron(TypeLiteral.get(type)); }

    default <T> ScopedBindingBuilder bindNeuron(TypeLiteral<T> typeLiteral) { return bindNeuron(Key.get(typeLiteral)); }

    default <T> ScopedBindingBuilder bindNeuron(Key<T> key) {
        return binder()
                .skipSources(BinderLike.class)
                .bind(key).toProvider(neuronProvider(key.getTypeLiteral()));
    }

    default <T> Provider<T> neuronProvider(Class<T> type) { return neuronProvider(TypeLiteral.get(type)); }

    @SuppressWarnings("unchecked")
    default <T> Provider<T> neuronProvider(final TypeLiteral<T> typeLiteral) {
        final Provider<Injector> injectorProvider = binder()
                .getProvider(Injector.class);
        final MembersInjector<T> membersInjector;
        if (typeLiteral.getRawType().isInterface()) {
            membersInjector = instance -> injectorProvider.get().injectMembers(instance);
        } else {
            membersInjector = binder().getMembersInjector(typeLiteral);
        }
        return Incubator
                .stub(NeuronProvider.class)
                .bind(NeuronProvider::injector).to(injectorProvider::get)
                .bind(NeuronProvider::membersInjector).to(membersInjector)
                .bind(NeuronProvider::typeLiteral).to(typeLiteral)
                .breed();
    }
}
