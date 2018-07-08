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

import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Type.getInternalName;

final class ASM implements Opcodes {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    /** Returns a class which proxies the given Neuron class or interface. */
    static <N> Class<? extends N> neuronProxyClass(final Class<? extends N> neuronClass, final List<Method> bindableMethods) {
        final Class<?> superclass;
        final Class<?>[] interfaces;
        if (neuronClass.isInterface()) {
            superclass = Object.class;
            interfaces = new Class<?>[] { neuronClass };
        } else {
            superclass = neuronClass;
            interfaces = NO_CLASSES;
        }
        final String implName = neuronClass.getName().concat("$$neuron");
        final ClassReader cr = classReader(neuronClass);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
        cr.accept(new NeuronClassVisitor(cw, superclass, interfaces, bindableMethods, internalName(implName)), SKIP_DEBUG);
        return defineSubclass(neuronClass, implName, cw.toByteArray());
    }

    private static ClassReader classReader(final Class<?> clazz) {
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(getInternalName(clazz).concat(".class"))) {
            return new ClassReader(in);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String internalName(String className) { return className.replace('.', '/'); }
}
