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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static global.namespace.neuron.di.internal.ASM.*;
import static org.objectweb.asm.Type.getInternalName;

final class ASMInterfaceVisitor extends ClassVisitor {

    private static final int ACC_PUBLIC_PRIVATE_PROTECTED = ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC;
    private static final int ACC_SUPER_ABSTRACT_SYNTHETIC = ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC;
    private static final String JAVA_LANG_OBJECT = getInternalName(Object.class);
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_VOID = "()V";

    private final Class<?> ifaceClass;
    private final String internalImplName;
    private final int modifiers;

    ASMInterfaceVisitor(final ClassVisitor cv,
                        final Class<?> ifaceClass,
                        final String internalImplName) {
        super(ASM5, cv);
        this.ifaceClass = ifaceClass;
        this.internalImplName = internalImplName;
        this.modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & ifaceClass.getModifiers();
    }

    @Override
    public void visit(int version,
                      int access,
                      String name,
                      String signature,
                      String superName,
                      String[] interfaces) {
        cv.visit(version,
                ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers,
                internalImplName,
                signature,
                JAVA_LANG_OBJECT,
                new String[]{ getInternalName(ifaceClass) });
    }

    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        return null;
    }

    @Override
    public void visitEnd() {
        insertConstructor();
        cv.visitEnd();
    }

    private void insertConstructor() {
        final MethodVisitor mv = cv.visitMethod(modifiers,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID,
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                JAVA_LANG_OBJECT,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID,
                false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
