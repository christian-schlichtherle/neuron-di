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
package global.namespace.neuron.di.internal.proxy;

import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

/**
 * Provides utilities for reflective lookup operations for generated proxy classes in this package.
 */
public class Proxies {

    public static final ClassLoader CLASS_LOADER;
    public static final String PACKAGE_NAME;
    public static final MethodHandles.Lookup PRIVATE_LOOKUP;

    static {
        final Class<Proxies> clazz = Proxies.class;
        CLASS_LOADER = clazz.getClassLoader();
        PACKAGE_NAME = clazz.getPackage().getName();
        try {
            //noinspection Since15
            PRIVATE_LOOKUP = privateLookupIn(clazz, lookup());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private Proxies() {
    }
}
