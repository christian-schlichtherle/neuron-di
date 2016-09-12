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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static global.namespace.neuron.di.internal.Reflection.defineSubtype;
import static org.objectweb.asm.Opcodes.*;

class Classes {

    private static final int ACC_PUBLIC_PRIVATE_PROTECTED =
            ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC;
    private static final int ACC_SUPER_ABSTRACT_SYNTHETIC =
            ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC;
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_RETURNS_VOID = "()V";
    private static final String JAVA_LANG_OBJECT_FILE =
            fileName(Object.class.getName());
    private static final String IMPLEMENTED_BY_NEURON_DI = "$$ImplementedByNeuronDI";

    private Classes() { }

    /**
     * Returns a class which implements the given interface.
     * The class implements any default methods of the interface so that they
     * are not abstract anymore.
     */
    static <T> Class<? extends T> classImplementsInterface(final Class<T> iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException();
        }

        final String ifaceName = iface.getName();
        final String className = ifaceName + IMPLEMENTED_BY_NEURON_DI;
        final int modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & iface.getModifiers();

        final ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_8, ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers, fileName(className), null, JAVA_LANG_OBJECT_FILE, new String[]{ fileName(ifaceName) });
        {
            final MethodVisitor mv = cw.visitMethod(modifiers, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_RETURNS_VOID, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, JAVA_LANG_OBJECT_FILE, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_RETURNS_VOID, false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return defineSubtype(iface, className, cw.toByteArray());
    }

    private static String fileName(String className) {
        return className.replace('.', '/');
    }
}
