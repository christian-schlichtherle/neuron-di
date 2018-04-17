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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** @author Christian Schlichtherle */
class Reflection8 {

    private Reflection8() { }

    private static final Method getClassLoadingLock, defineClass;

    static {
        final Class<ClassLoader> classLoaderClass = ClassLoader.class;
        try {
            getClassLoadingLock = classLoaderClass
                    .getDeclaredMethod("getClassLoadingLock", String.class);
            getClassLoadingLock.setAccessible(true);
            defineClass = classLoaderClass
                    .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <C> Class<? extends C> defineSubclass(final Class<C> clazz, final String name, final byte[] b) {
        final ClassLoader cl = clazz.getClassLoader();
        try {
            synchronized (getClassLoadingLock.invoke(cl, name)) {
                return (Class<? extends C>) defineClass.invoke(cl, name, b, 0, b.length);
            }
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}