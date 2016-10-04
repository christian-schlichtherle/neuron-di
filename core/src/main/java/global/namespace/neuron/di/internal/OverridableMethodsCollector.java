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

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static global.namespace.neuron.di.internal.Reflection.traverse;
import static java.lang.reflect.Modifier.*;

final class OverridableMethodsCollector {

    private static final int PRIVATE_STATIC_FINAL = PRIVATE | STATIC | FINAL;
    private static final int PROTECTED_PUBLIC = PROTECTED | PUBLIC;

    private final Package originPackage;

    final Map<String, Method> methods = new LinkedHashMap<>();

    OverridableMethodsCollector(final Class<?> origin) {
        this.originPackage = origin.getPackage();
        add(origin);
    }

    OverridableMethodsCollector add(final Class<?> clazz) {
        traverse(c -> {
            for (final Method method : c.getDeclaredMethods()) {
                final int modifiers = method.getModifiers();
                if (0 == (modifiers & PRIVATE_STATIC_FINAL) &&
                        (0 != (modifiers & PROTECTED_PUBLIC) || c.getPackage() == originPackage)) {
                    methods.putIfAbsent(method.getName() + Type.getMethodDescriptor(method), method);
                }
            }
        }).accept(clazz);
        return this;
    }
}
