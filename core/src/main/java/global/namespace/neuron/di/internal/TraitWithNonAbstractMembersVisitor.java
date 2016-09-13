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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static global.namespace.neuron.di.internal.ASM.*;
import static global.namespace.neuron.di.internal.Reflection.traverse;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;

final class TraitWithNonAbstractMembersVisitor extends ClassVisitor {

    private final Deque<TraitWithNonAbstractMembers> traits = new LinkedList<>();

    private final Class<?> traitClass;
    private final String internalImplName;
    private final int modifiers;
    private final Map<MethodSignature, Method> methods;

    TraitWithNonAbstractMembersVisitor(final ClassVisitor cv,
                                       final Class<?> traitClass,
                                       final String internalImplName) {
        super(ASM5, cv);
        this.traitClass = traitClass;
        this.internalImplName = internalImplName;
        this.modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & traitClass.getModifiers();
        traverse(ifaceClass -> TraitWithNonAbstractMembers
                .test(ifaceClass)
                .ifPresent(traits::addFirst)
        ).accept(traitClass);
        methods = traits.stream()
                .flatMap(trait -> trait.declaredNonAbstractMembers().entrySet().stream())
                .collect(
                        HashMap::new,
                        (map, entry) -> map.put(new MethodSignature(entry.getKey()), entry.getValue()),
                        Map::putAll
                );
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        cv.visit(version,
                ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers,
                internalImplName,
                signature,
                JAVA_LANG_OBJECT,
                new String[]{ getInternalName(traitClass) });
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String desc,
                                     final String signature,
                                     final String[] exceptions) {
        if (methods.containsKey(new MethodSignature(name, desc))) {
            return null;
        } else {
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        insertMethods();
        insertConstructor();
        cv.visitEnd();
    }

    private void insertMethods() {
        final ClassVisitor target = cv;
        traits.forEach(trait -> {
            final ClassReader cr = classReader(trait.ifaceClass());
            cr.accept(new ClassVisitor(ASM5) {

                @Override
                public MethodVisitor visitMethod(final int access,
                                                 final String name,
                                                 final String desc,
                                                 final String signature,
                                                 final String[] exceptions) {
                    final Method implementation = methods
                            .remove(new MethodSignature(name, desc));
                    if (null != implementation) {
                        final int implementationParameterCount =
                                implementation.getParameterCount();
                        final MethodVisitor mv = target.visitMethod(
                                ACC_SYNTHETIC | ~ACC_ABSTRACT & access,
                                name,
                                desc,
                                signature,
                                exceptions);
                        mv.visitCode();
                        for (int i = 0; i < implementationParameterCount; i++) {
                            mv.visitVarInsn(ALOAD, i);
                        }
                        mv.visitMethodInsn(INVOKESTATIC,
                                getInternalName(implementation.getDeclaringClass()),
                                implementation.getName(),
                                getMethodDescriptor(implementation),
                                false);
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(implementationParameterCount,
                                implementationParameterCount);
                        mv.visitEnd();
                    }
                    return null;
                }
            }, SKIP_DEBUG | SKIP_CODE | SKIP_FRAMES);
        });
    }

    private void insertConstructor() {
        final MethodVisitor mv = cv.visitMethod(ACC_SYNTHETIC | modifiers,
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
        for (final TraitWithNonAbstractMembers trait : traits) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC,
                    getInternalName(trait.classClass()),
                    "$init$",
                    "(L" + getInternalName(trait.ifaceClass()) + ";)V",
                    false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
