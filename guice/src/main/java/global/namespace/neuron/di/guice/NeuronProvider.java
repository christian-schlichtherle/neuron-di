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

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.api.Neuron;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Supplier;

@Neuron
abstract class NeuronProvider<T> implements Provider<T> {

    abstract Injector injector();

    abstract Class<T> runtimeClass();

    public T get() { return Incubator.breed(runtimeClass(), this::resolve); }

    private Supplier<?> resolve(final Method method) {
        final Type type = method.getGenericReturnType();
        final Key<?> key = Arrays
                .stream(method.getAnnotations())
                .filter(NeuronProvider::isQualifierOrBindingAnnotation)
                .findFirst()
                .<Key<?>>map(annotation -> Key.get(type, annotation))
                .orElseGet(() -> Key.get(type));
        final Provider<?> provider = injector().getProvider(key);
        return provider::get;
    }

    private static boolean isQualifierOrBindingAnnotation(final Annotation annotation) {
        final Class<? extends Annotation> type = annotation.annotationType();
        return type.isAnnotationPresent(Qualifier.class) ||
                type.isAnnotationPresent(BindingAnnotation.class);
    }
}
