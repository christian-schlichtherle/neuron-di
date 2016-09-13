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
import java.util.*;
import java.util.stream.Stream;

interface TraitWithNonAbstractMembers {

    static Optional<TraitWithNonAbstractMembers> test(final Class<?> ifaceClass) {
        final ClassLoader cl = ifaceClass.getClassLoader();
        if (null != cl) {
            try {
                return Optional.of(new TraitWithNonAbstractMembers() {

                    final Class<?> classClass =
                            cl.loadClass(ifaceClass.getName() + "$class");

                    @Override
                    public Class<?> ifaceClass() {
                        return ifaceClass;
                    }

                    @Override
                    public Class<?> classClass() {
                        return classClass;
                    }
                });
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Optional.empty();
    }

    default Map<Method, Method> declaredNonAbstractMembers() {
        return Stream.of(classClass().getDeclaredMethods()).collect(
                LinkedHashMap::new,
                (map, method) -> {
                    final String name = method.getName();
                    if ("$init$".equals(name)) {
                        return;
                    }
                    final List<Class<?>> parameterTypes =
                            new ArrayList<>(Arrays.asList(method.getParameterTypes()));
                    parameterTypes.remove(0);
                    try {
                        map.put(ifaceClass().getDeclaredMethod(
                                name,
                                parameterTypes.toArray(new Class<?>[parameterTypes.size()])),
                                method);
                    } catch (NoSuchMethodException ignored) {
                        // This may happen if the implementation method has
                        // the name "$init$" or access modifiers private,
                        // final etc.
                    }
                },
                Map::putAll
        );
    }

    Class<?> ifaceClass();

    Class<?> classClass();
}
