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
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static global.namespace.neuron.di.internal.Reflection.associatedClassLoader;
import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.Type.getInternalName;

final class ASM implements Opcodes {

    private static final String SHIM = "$$shim";
    private static final String NEURON = "$$neuron";

    /**
     * Returns a class which implements the given Java interface.
     * The class implements any default methods of the interface.
     */
    static <T> Class<? extends T> classImplementingJava(final Class<T> ifaceClass) {
        if (!ifaceClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        final String implName = ifaceClass.getName() + SHIM;
        final ClassReader cr = classReader(ifaceClass);
        final ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ASMInterfaceVisitor(cw, ifaceClass, internalName(implName)), SKIP_DEBUG);
        return defineSubclass(ifaceClass, implName, cw.toByteArray());
    }

    /**
     * Returns a class which implements the given Neuron class or interface.
     */
    static <T> Class<? extends T> neuronClass(final Class<T> superClass, final List<Method> suppliers) {
        final String implName = superClass.getName() + NEURON;
        final ClassReader cr = classReader(superClass);
        final ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ASMNeuronVisitor(cw, superClass, internalName(implName), suppliers), SKIP_DEBUG);
        return defineSubclass(superClass, implName, cw.toByteArray());
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
