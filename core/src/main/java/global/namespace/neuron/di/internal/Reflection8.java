/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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

import global.namespace.neuron.di.internal.proxy.Proxies;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

class Reflection8 {

    private static final Unsafe unsafe;

    static {
        try {
            final Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private Reflection8() {
    }

    @SuppressWarnings("unchecked")
    static <C> Class<? extends C> defineSubclass(final Class<C> clazz, final String name, final byte[] b) {
        final ClassLoader loader = clazz.getClassLoader();
        return (Class<? extends C>) unsafe.defineClass(
                name,
                b,
                0,
                b.length,
                null != loader ? loader : Proxies.CLASS_LOADER,
                clazz.getProtectionDomain()
        );
    }
}
