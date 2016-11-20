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

import global.namespace.neuron.di.java.DependencyProvider;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static global.namespace.neuron.di.internal.Reflection.boxed;
import static java.lang.Math.max;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

class NeuronClassVisitor extends ClassVisitor {

    private static final int ACC_ABSTRACT_INTERFACE = ACC_ABSTRACT | ACC_INTERFACE;
    private static final int ACC_PRIVATE_SYNTHETIC = ACC_PRIVATE | ACC_SYNTHETIC;
    private static final int ACC_FINAL_SUPER_SYNTHETIC = ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC;
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_VOID_DESC = "()V";
    private static final String OBJECT_DESC = "Ljava/lang/Object;";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC = "()" + OBJECT_DESC;

    static final String PROVIDER = "$provider";
    private static final String SUPER = "super$";

    private static final Type acceptsNothingAndReturnsObjectType = getType(ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC);
    private static final String dependencyProviderName = getInternalName(DependencyProvider.class);
    private static final String dependencyProviderDesc = getDescriptor(DependencyProvider.class);
    private static final Handle metaFactoryHandle = new Handle(H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            false);

    private final String superName, neuronProxyName, neuronProxyDesc;
    private final String[] interfaces;
    private final List<Method> providerMethods;

    NeuronClassVisitor(final ClassVisitor cv,
                       final Class<?> superclass,
                       final Class<?>[] interfaces,
                       final List<Method> providerMethods,
                       final String neuronProxyName) {
        super(ASM5, cv);
        this.superName = getInternalName(superclass);
        int i = interfaces.length;
        this.interfaces = new String[i];
        while (0 <= --i) {
            this.interfaces[i] = getInternalName(interfaces[i]);
        }
        this.providerMethods = providerMethods;
        this.neuronProxyName = neuronProxyName;
        this.neuronProxyDesc = "L" + neuronProxyName + ";";
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      String superName,
                      String[] interfaces) {
        cv.visit(max(version, V1_8),
                access & ~ACC_ABSTRACT_INTERFACE | ACC_FINAL_SUPER_SYNTHETIC,
                this.neuronProxyName,
                null,
                this.superName,
                this.interfaces);
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
        final MethodVisitor mv = cv.visitMethod(ACC_PRIVATE_SYNTHETIC,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID_DESC,
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                superName,
                CONSTRUCTOR_NAME,
                ACCEPTS_NOTHING_AND_RETURNS_VOID_DESC,
                false);
        boolean nonAbstract = false;
        for (final Method method : providerMethods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                nonAbstract = true;
                final String name = method.getName();
                final String desc = getMethodDescriptor(method);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInvokeDynamicInsn("get",
                        "(" + neuronProxyDesc + ")" + dependencyProviderDesc,
                        metaFactoryHandle,
                        acceptsNothingAndReturnsObjectType,
                        new Handle(H_INVOKESPECIAL,
                                neuronProxyName,
                                SUPER + name,
                                desc,
                                false),
                        acceptsNothingAndReturnsObjectType);
                mv.visitFieldInsn(PUTFIELD, neuronProxyName, name + PROVIDER, dependencyProviderDesc);
            }
        }
        mv.visitInsn(RETURN);
        if (nonAbstract) {
            mv.visitMaxs(2, 1);
        } else {
            mv.visitMaxs(1, 1);
        }
        mv.visitEnd();
    }

    private void insertMethods() {
        for (final Method method : providerMethods) {
            new Object() {

                final int access = method.getModifiers() & ~ACC_ABSTRACT | ACC_SYNTHETIC;
                final String name = method.getName();
                final String desc = getMethodDescriptor(method);

                final Class<?> returnType = method.getReturnType();
                final String returnTypeName = getInternalName(returnType);
                final String returnTypeDesc = getDescriptor(returnType);

                final Class<?> boxedReturnType = boxed(returnType);
                final String boxedReturnTypeName = getInternalName(boxedReturnType);
                final String boxedReturnTypeDesc = getDescriptor(boxedReturnType);

                final int returnOpCode = returnOpCode(method);

                void apply() {
                    generateProxyField();
                    generateProxyCallMethod();
                    generateSuperCallMethod();
                }

                void generateProxyField() {
                    cv      .visitField(ACC_PRIVATE_SYNTHETIC, name + PROVIDER, dependencyProviderDesc, null, null)
                            .visitEnd();
                }

                void generateProxyCallMethod() {
                    final MethodVisitor mv = beginMethod(name);
                    mv.visitFieldInsn(GETFIELD, neuronProxyName, name + PROVIDER, dependencyProviderDesc);
                    mv.visitMethodInsn(INVOKEINTERFACE, dependencyProviderName, "get", ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC, true);
                    if (!boxedReturnType.isAssignableFrom(Object.class)) {
                        mv.visitTypeInsn(CHECKCAST,
                                boxedReturnType.isArray() ? boxedReturnTypeDesc : boxedReturnTypeName);
                    }
                    endMethod(mv);
                }

                void generateSuperCallMethod() {
                    if (!Modifier.isAbstract(method.getModifiers())) {
                        final MethodVisitor mv = beginMethod(SUPER + name);
                        mv.visitMethodInsn(INVOKESPECIAL,
                                0 == interfaces.length ? superName : ownerName(),
                                name,
                                desc,
                                0 != interfaces.length);
                        endMethod(mv);
                    }
                }

                String ownerName() { return getInternalName(method.getDeclaringClass()); }

                MethodVisitor beginMethod(final String name) {
                    final MethodVisitor mv = cv.visitMethod(access, name, desc, null, null);
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    return mv;
                }

                void endMethod(final MethodVisitor mv) {
                    if (returnType != boxedReturnType ) {
                        assert !returnType.isArray();
                        mv.visitMethodInsn(INVOKEVIRTUAL,
                                boxedReturnTypeName,
                                returnTypeName + "Value",
                                "()" + returnTypeDesc,
                                false);
                        mv.visitInsn(returnOpCode);
                        mv.visitMaxs(2, 1);
                    } else {
                        mv.visitInsn(returnOpCode);
                        mv.visitMaxs(1, 1);
                    }
                    mv.visitEnd();
                }
            }.apply();
        }
    }

    private static int returnOpCode(final Method method) {
        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if (returnType == Float.TYPE) {
                return FRETURN;
            } else if (returnType == Double.TYPE) {
                return DRETURN;
            } else if (returnType == Long.TYPE) {
                return LRETURN;
            } else if (returnType == Void.TYPE) {
                throw new IllegalStateException("Method has void return type: " + method);
            } else {
                return IRETURN;
            }
        } else {
            if (returnType == Void.class) {
                throw new IllegalStateException("Method has Void return type: " + method);
            } else {
                return ARETURN;
            }
        }
    }
}
