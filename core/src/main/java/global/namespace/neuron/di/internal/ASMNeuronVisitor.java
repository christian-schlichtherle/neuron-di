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
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static global.namespace.neuron.di.internal.Reflection.boxed;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getInternalName;

class ASMNeuronVisitor extends ClassVisitor {

    private static final int ACC_ABSTRACT_INTERFACE = ACC_ABSTRACT | ACC_INTERFACE;
    private static final int ACC_SUPER_SYNTHETIC = ACC_SUPER | ACC_SYNTHETIC;
    private static final int ACC_PRIVATE_SYNTHETIC = ACC_PRIVATE | ACC_SYNTHETIC;
    private static final int ACC_PUBLIC_SYNTHETIC = ACC_PUBLIC | ACC_SYNTHETIC;
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_VOID = "()V";

    private static final String objectName = getInternalName(Object.class);

    private final Class<?> superClass;
    private final String superName, implName;
    private final Map<String, Method> suppliers;

    ASMNeuronVisitor(final ClassVisitor visitor,
                     final Class<?> superClass,
                     final String implName,
                     final Set<Method> suppliers) {
        super(ASM5, visitor);
        this.superClass = superClass;
        this.superName = getInternalName(superClass);
        this.implName = implName;
        this.suppliers = suppliers.stream().collect(Collectors.toMap(Method::getName, Function.identity()));
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      String superName,
                      String[] interfaces) {
        if (superClass.isInterface()) {
            superName = objectName;
            interfaces = new String[]{ this.superName };
        } else {
            superName = this.superName;
            interfaces = null;
        }
        cv.visit(version,
                access & ~ACC_ABSTRACT_INTERFACE | ACC_SUPER_SYNTHETIC,
                implName,
                signature,
                superName,
                interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return null;
    }

    @Override
    public void visitEnd() {
        insertConstructor();
        insertMethods();
        cv.visitEnd();
    }

    private void insertConstructor() {
        final String superName = superClass.isInterface() ? objectName : this.superName;
        final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC_SYNTHETIC,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID,
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                superName,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID,
                false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void insertMethods() {
        for (final Method method : suppliers.values()) {
            new Object() {

                final String methodName = method.getName();
                final Class<?> returnType = method.getReturnType();
                final String returnTypeName = getInternalName(returnType);
                final String returnTypeDesc = Type.getDescriptor(returnType);
                final Class<?> boxedReturnType = boxed(returnType);
                final String boxedReturnTypeName = getInternalName(boxedReturnType);
                final String boxedReturnTypeDesc = Type.getDescriptor(boxedReturnType);
                final int access = method.getModifiers() & ~ACC_ABSTRACT | ACC_SYNTHETIC;
                final String desc = Type.getMethodDescriptor(method);
                final int returnOpCode;

                {
                    if (returnType.isPrimitive()) {
                        if (returnType == Float.TYPE) {
                            returnOpCode = FRETURN;
                        } else if (returnType == Double.TYPE) {
                            returnOpCode = DRETURN;
                        } else if (returnType == Long.TYPE) {
                            returnOpCode = LRETURN;
                        } else if (returnType == Void.TYPE) {
                            throw new IllegalStateException("Method has void return type: " + method);
                        } else {
                            returnOpCode = IRETURN;
                        }
                    } else {
                        if (returnType == Void.class) {
                            throw new IllegalStateException("Method has Void return type: " + method);
                        } else {
                            returnOpCode = ARETURN;
                        }
                    }
                }

                void run() {
                    generateSupplierField();
                    generateSupplierCallMethod();
                    generateSuperCallMethod();
                }

                void generateSupplierField() {
                    cv  .visitField(ACC_PRIVATE_SYNTHETIC,
                            methodName + "$supplier",
                            "Ljava/util/function/Supplier;",
                            null,
                            null)
                        .visitEnd();
                }

                void generateSupplierCallMethod() {
                    final MethodVisitor mv = beginMethod(methodName);
                    mv.visitFieldInsn(GETFIELD, implName, methodName + "$supplier", "Ljava/util/function/Supplier;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Supplier", "get", "()Ljava/lang/Object;", true);
                    if (!boxedReturnType.isAssignableFrom(Object.class)) {
                        mv.visitTypeInsn(CHECKCAST, boxedReturnType.isArray() ? boxedReturnTypeDesc : boxedReturnTypeName);
                    }
                    endMethod(mv);
                }

                void generateSuperCallMethod() {
                    final MethodVisitor mv = beginMethod(methodName + "$super");
                    mv.visitMethodInsn(INVOKESPECIAL, superName, methodName, desc, false);
                    endMethod(mv);
                }

                MethodVisitor beginMethod(final String name) {
                    final MethodVisitor mv = cv.visitMethod(access,
                            name,
                            desc,
                            null,
                            null);
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    return mv;
                }

                void endMethod(final MethodVisitor mv) {
                    if (returnType != boxedReturnType ) {
                        assert !returnType.isArray();
                        mv.visitMethodInsn(INVOKEVIRTUAL, boxedReturnTypeName, returnTypeName + "Value", "()" + returnTypeDesc, false);
                    }
                    mv.visitInsn(returnOpCode);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                }
            }.run();
        }
    }
}
