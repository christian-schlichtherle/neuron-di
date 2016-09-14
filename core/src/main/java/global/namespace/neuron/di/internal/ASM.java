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

import static global.namespace.neuron.di.internal.Reflection.associatedClassLoader;
import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.Type.getInternalName;

final class ASM implements Opcodes {

    static final int ACC_PUBLIC_PRIVATE_PROTECTED = ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC;

    static final int ACC_SUPER_ABSTRACT_SYNTHETIC = ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC;

    static final String CONSTRUCTOR_NAME = "<init>";

    static final String ACCEPTS_NOTHING_AND_RETURNS_VOID = "()V";

    static final String JAVA_LANG_OBJECT = getInternalName(Object.class);

    private static final String IMPLEMENTED_BY_NEURON_DI = "$$ImplementedByNeuronDI";

    private static String internalName(String className) {
        return className.replace('.', '/');
    }

    static <T> ClassReader classReader(final Class<T> clazz) {
        try (InputStream in = associatedClassLoader(clazz)
                .getResourceAsStream(getInternalName(clazz) + ".class")) {
            return new ClassReader(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a class which implements the given Java interface.
     * The class implements any default methods of the interface.
     */
    static <T> Class<? extends T> classImplementingJava(final Class<T> ifaceClass) {
        if (!ifaceClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        final String implName = ifaceClass.getName() + IMPLEMENTED_BY_NEURON_DI;
        final ClassReader cr = classReader(ifaceClass);
        final ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new InterfaceVisitor(cw, ifaceClass, internalName(implName)), SKIP_DEBUG);
        return defineSubclass(ifaceClass, implName, cw.toByteArray());
    }
}
