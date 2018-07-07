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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static global.namespace.neuron.di.internal.Reflection.traverse;
import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

final class OverridableMethodsCollector {

    // VOLATILE methods are bridge methods, e.g. for use in generic classes.
    private static final int PRIVATE_STATIC_VOLATILE = PRIVATE | STATIC | VOLATILE;

    private static final int PROTECTED_PUBLIC = PROTECTED | PUBLIC;

    private final Package pkg;

    OverridableMethodsCollector(final Package pkg) { this.pkg = pkg; }

    Collection<Method> collect(final Class<?> clazz) {
        final Map<String, Method> methods = new LinkedHashMap<>();
        traverse(t -> {
            for (final Method method : t.getDeclaredMethods()) {
                final int modifiers = method.getModifiers();
                if (0 == (modifiers & PRIVATE_STATIC_VOLATILE) &&
                        (0 != (modifiers & PROTECTED_PUBLIC) || t.getPackage() == pkg)) {
                    methods.putIfAbsent(signature(method), method);
                }
            }
        }).accept(clazz);
        final Collection<Method> values = methods.values();
        values.removeIf(method -> 0 != (method.getModifiers() & FINAL));
        return values;
    }

    private static String signature(Method method) {
        return method.getName() + methodDescriptorWithoutReturnType(method);
    }

    private static String methodDescriptorWithoutReturnType(Method method) {
        return getMethodDescriptor(method).replaceAll("\\).*", ")");
    }
}
