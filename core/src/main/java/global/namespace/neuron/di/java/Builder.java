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
package global.namespace.neuron.di.java;

import global.namespace.neuron.di.internal.RealBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static global.namespace.neuron.di.java.Reflection.find;
import static global.namespace.neuron.di.java.Reflection.isAbstract;
import static java.util.Optional.*;

/** @author Christian Schlichtherle */
public final class Builder {

    private Builder() { }

    public static <T> T build(Class<T> clazz) {
        return build(clazz, method -> {
            if (isAbstract(method)) {
                final Class<?> returnType = method.getReturnType();
                return of(() -> build(returnType));
            } else {
                return empty();
            }
        });
    }

    public static <T> T build(Class<T> clazz, MethodBinding binding) {
        return RealBuilder.build(clazz, binding);
    }

    public static <T> Wire<T> wire(Class<T> clazz) {
        return new Wire<T>() {

            List<Map.Entry<DependencyResolver<T, ?>, DependencyResolver<? super T, ?>>> bindings = new LinkedList<>();

            boolean partial;

            Object delegate;

            @Override
            public Wire<T> partial(final boolean value) {
                this.partial = value;
                return this;
            }

            @Override
            public <U> Bind<T, U> bind(final DependencyResolver<T, U> methodReference) {
                return resolver -> {
                    bindings.add(new SimpleImmutableEntry<>(methodReference, resolver));
                    return this;
                };
            }

            @Override
            public T using(final Object delegate) {
                partial = true;
                this.delegate = delegate;
                return build();
            }

            @Override
            public T build() {
                return new Object() {

                    final T neuron = RealBuilder.build(clazz, new MethodBinding() {

                        final Map<Method, DependencyResolver<? super T, ?>> resolvedBindings =
                                Resolver.resolve(clazz, bindings);

                        @Override
                        public Optional<DependencyProvider<?>> apply(final Method method) {
                            final Optional<DependencyProvider<?>> optionalDependencyProvider =
                                    ofNullable(resolvedBindings.get(method)).map(resolver -> () -> resolver.apply(neuron));
                            if (optionalDependencyProvider.isPresent()) {
                                return optionalDependencyProvider;
                            } else if (!isAbstract(method)) {
                                return empty();
                            } else if (!partial) {
                                throw new BreedingException(
                                        "Partial binding is disabled and no binding is defined for synapse method: " + method);
                            } else if (null != delegate) {
                                final String member = method.getName();
                                final MethodHandle handle = find(member).in(delegate).orElseThrow(() ->
                                        new BreedingException("Illegal binding: A member named `" + member + "` neither exists in `" + delegate.getClass() + "` nor in any of its interfaces and superclasses."));
                                return of(handle::invokeExact);
                            } else {
                                return of(() -> Incubator.breed(method.getReturnType()));
                            }
                        }
                    });
                }.neuron;
            }
        };
    }

    public interface Wire<T> {

        Wire<T> partial(boolean value);

        <U> Bind<T, U> bind(DependencyResolver<T, U> methodReference);

        T using(Object delegate);

        T build();
    }

    public interface Bind<T, D> {

        default Wire<T> to(D value) { return to(neuron -> value); }

        default Wire<T> to(DependencyProvider<? extends D> provider) { return to(neuron -> provider.get()); }

        Wire<T> to(DependencyResolver<? super T, ? extends D> resolver);
    }
}
