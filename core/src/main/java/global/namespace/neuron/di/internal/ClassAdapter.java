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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static global.namespace.neuron.di.internal.ASM.interface2class;
import static global.namespace.neuron.di.internal.Reflection.hasCachingEligibleDefaultMethods;
import static global.namespace.neuron.di.internal.Reflection.isTraitWithNonAbstractMembers;

/**
 * Adapts a consumer which accepts a class object reflecting a super class
 * and an array of class objects reflecting interfaces to a consumer which
 * accepts a class object reflecting a class or interface.
 */
final class ClassAdapter implements Consumer<Class<?>> {

    private static Class<?>[] NO_CLASSES = new Class<?>[0];

    private final BiConsumer<Class<?>, Class<?>[]> consumer;

    /**
     * @param consumer a consumer which accepts a class object reflecting a
     *                 super class and an array of class objects reflecting
     *                 interfaces.
     */
    ClassAdapter(final BiConsumer<Class<?>, Class<?>[]> consumer) {
        this.consumer = consumer;
    }

    /** Calls the adapted consumer and returns its value. */
    @Override
    public void accept (Class<?> runtimeClass) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (runtimeClass.isInterface()) {
            if (isTraitWithNonAbstractMembers(runtimeClass)) {
                throw new UnsupportedOperationException("Trait with non-abstract members: " + runtimeClass);
            } else if (hasCachingEligibleDefaultMethods(runtimeClass)) {
                superclass = interface2class(runtimeClass);
                interfaces = NO_CLASSES;
            } else {
                superclass = Object.class;
                interfaces = new Class<?>[] { runtimeClass };
            }
        } else {
            superclass = runtimeClass;
            interfaces = NO_CLASSES;
        }
        consumer.accept(superclass, interfaces);
    }
}