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
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.DependencyProvider;
import global.namespace.neuron.di.java.Incubator;
import global.namespace.neuron.di.java.Neuron;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/** @author Christian Schlichtherle */
@Neuron
abstract class NeuronProvider<T> implements Provider<T> {

    @Caching
    abstract Injector injector();

    abstract TypeLiteral<T> typeLiteral();

    abstract MembersInjector<T> membersInjector();

    @SuppressWarnings("unchecked")
    public T get() {
        final T instance = (T) Incubator.breed(typeLiteral().getRawType(), this::binding);
        membersInjector().injectMembers(instance);
        return instance;
    }

    private DependencyProvider<?> binding(final Method method) {
        final TypeLiteral<?> returnTypeLiteral = typeLiteral().getReturnType(method);
        final Key<?> returnKey = Arrays
                .stream(method.getDeclaredAnnotations())
                .filter(NeuronProvider::isQualifierOrBindingAnnotation)
                .findFirst()
                .<Key<?>>map(annotation -> Key.get(returnTypeLiteral, annotation))
                .orElseGet(() -> Key.get(returnTypeLiteral));
        return injector().getProvider(returnKey)::get;
    }

    private static boolean isQualifierOrBindingAnnotation(final Annotation annotation) {
        final Class<? extends Annotation> type = annotation.annotationType();
        return type.isAnnotationPresent(Qualifier.class) ||
                type.isAnnotationPresent(BindingAnnotation.class);
    }
}
