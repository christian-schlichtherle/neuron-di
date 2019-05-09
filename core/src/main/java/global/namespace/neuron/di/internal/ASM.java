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

import global.namespace.neuron.di.internal.proxy.Proxies;
import global.namespace.neuron.di.java.BreedingException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static java.util.Optional.ofNullable;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Type.getInternalName;

final class ASM implements Opcodes {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    private static final String PROXIES_PACKAGE_PREFIX = Proxies.PACKAGE_NAME + ".$";

    /** Returns a class which proxies the given class or interface. */
    static <N> Class<? extends N> proxyClass(final Class<? extends N> clazz, final List<Method> bindableMethods) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (clazz.isInterface()) {
            superclass = Object.class;
            interfaces = new Class<?>[] { clazz };
        } else {
            superclass = clazz;
            interfaces = NO_CLASSES;
        }
        final String proxyName = null != clazz.getClassLoader()
                ? clazz.getName().concat("$$proxy")
                : PROXIES_PACKAGE_PREFIX.concat(clazz.getName().replace('.', '$'));
        final ClassReader cr = classReader(clazz);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
        cr.accept(new ProxyClassVisitor(cw, internalName(proxyName), superclass, interfaces, bindableMethods), SKIP_DEBUG);
        return defineSubclass(clazz, proxyName, cw.toByteArray());
    }

    private static ClassReader classReader(final Class<?> clazz) {
        try (InputStream in = inputStream(clazz)) {
            return new ClassReader(in);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static InputStream inputStream(final Class<?> clazz) {
        final String resourceName = getInternalName(clazz).concat(".class");
        return ofNullable(clazz.getClassLoader())
                .map(cl -> cl.getResourceAsStream(resourceName))
                .orElseGet(() -> ofNullable(ClassLoader.getSystemResourceAsStream(resourceName))
                        .orElseThrow(() -> new BreedingException("Class not found: " + clazz.getName())));
    }

    private static String internalName(String className) { return className.replace('.', '/'); }
}
