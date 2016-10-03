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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import static global.namespace.neuron.di.internal.Reflection.associatedClassLoader;
import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.Type.getInternalName;

final class ASM implements Opcodes {

    private static final String NEURON = "$$neuron";

    /** Returns a class which proxies the given Neuron class or interface. */
    static <T> Class<? extends T> neuronProxyClass(final Class<T> superclass, final Class<?>[] interfaces, final List<Method> proxiedMethods) {
        @SuppressWarnings("unchecked")
        final Class<T> clazz = 0 == interfaces.length ? superclass : (Class<T>) interfaces[0];
        final String implName = clazz.getName() + NEURON;
        final ClassReader cr = classReader(clazz);
        final ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ASMNeuronClassVisitor(cw, superclass, interfaces, proxiedMethods, internalName(implName)), SKIP_DEBUG);
        return defineSubclass(clazz, implName, cw.toByteArray());
    }

    private static <T> ClassReader classReader(final Class<T> clazz) {
        try (InputStream in = associatedClassLoader(clazz)
                .getResourceAsStream(getInternalName(clazz) + ".class")) {
            return new ClassReader(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String internalName(String className) { return className.replace('.', '/'); }
}
