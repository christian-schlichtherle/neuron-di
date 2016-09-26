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

    /** Returns the underlying binder. */
    Binder binder();

    /**
     * Binds a constant with the qualifier annotation {@code @@Named(name)}, where {@code name} is the given name.
     * This is an abbreviation for {@code bindConstant().annotatedWith(Names.named(named))}.
     */
    default ConstantBindingBuilder bindConstantNamed(String name) {
        return binder().bindConstant().annotatedWith(named(name));
    }

    /**
     * Binds one or more neuron classes or interfaces using the given classes.
     * This is an abbreviation for multiple calls to {@link #bindNeuron(Class)}.
     *
     * @since Neuron DI 3.2
     */
    default void bindNeurons(final Class<?> neuron, final Class<?>... neurons) {
        bindNeuron(neuron);
        for (Class<?> clazz : neurons) {
            bindNeuron(clazz);
        }
    }

    /**
     * Binds one or more neuron classes or interfaces using the given type literals.
     * This is an abbreviation for multiple calls to {@link #bindNeuron(TypeLiteral)}.
     *
     * @since Neuron DI 3.2
     */
    default void bindNeurons(final TypeLiteral<?> neuron, final TypeLiteral<?>... neurons) {
        bindNeuron(neuron);
        for (TypeLiteral<?> clazz : neurons) {
            bindNeuron(clazz);
        }
    }

    /**
     * Binds one or more neuron classes or interfaces using the given keys.
     * This is an abbreviation for multiple calls to {@link #bindNeuron(Key)}.
     *
     * @since Neuron DI 3.2
     */
    default void bindNeurons(final Key<?> neuron, final Key<?>... neurons) {
        bindNeuron(neuron);
        for (Key<?> clazz : neurons) {
            bindNeuron(clazz);
        }
    }

    /**
     * Binds a neuron class or interface using the given class.
     * This is an abbreviation for {@code bind(type).toProvider(neuronProvider(type)}.
     */
    default <T> ScopedBindingBuilder bindNeuron(Class<T> type) {
        return binder()
                .skipSources(BinderLike.class)
                .bind(type).toProvider(neuronProvider(type));
    }

    /**
     * Binds a neuron class or interface using the given type literal.
     * This is an abbreviation for {@code bind(typeLiteral).toProvider(neuronProvider(typeLiteral)}.
     */
    default <T> ScopedBindingBuilder bindNeuron(TypeLiteral<T> typeLiteral) {
        return binder()
                .skipSources(BinderLike.class)
                .bind(typeLiteral).toProvider(neuronProvider(typeLiteral));
    }

    /**
     * Binds a neuron class or interface using the given key.
     * This is an abbreviation for {@code bind(key).toProvider(neuronProvider(key.getTypeLiteral())}.
     */
    default <T> ScopedBindingBuilder bindNeuron(Key<T> key) { return bindNeuron(key.getTypeLiteral()); }

    /** Returns a provider for neurons of the given class. */
    default <T> Provider<T> neuronProvider(Class<T> type) { return neuronProvider(TypeLiteral.get(type)); }

    /** Returns a provider for neurons of the given type literal. */
    @SuppressWarnings("unchecked")
    default <T> Provider<T> neuronProvider(final TypeLiteral<T> typeLiteral) {
        final Provider<Injector> injectorProvider = binder().getProvider(Injector.class);
        final MembersInjector<T> membersInjector;
        if (typeLiteral.getRawType().isInterface()) {
            membersInjector = instance -> injectorProvider.get().injectMembers(instance); // a no-op, effectively
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
