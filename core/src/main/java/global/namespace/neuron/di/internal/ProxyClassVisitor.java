/*
 * Copyright © 2016 - 2020 Schlichtherle IT Services
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

import global.namespace.neuron.di.java.BreedingException;
import global.namespace.neuron.di.java.DependencyProvider;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.List;

import static global.namespace.neuron.di.internal.Reflection.boxed;
import static java.lang.Math.max;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

final class ProxyClassVisitor extends ClassVisitor {

    private static final int ACC_ABSTRACT_INTERFACE = ACC_ABSTRACT | ACC_INTERFACE;
    private static final int ACC_ABSTRACT_NATIVE = ACC_ABSTRACT | ACC_NATIVE;
    private static final int ACC_PRIVATE_SYNTHETIC = ACC_PRIVATE | ACC_SYNTHETIC;
    private static final int ACC_FINAL_SUPER_SYNTHETIC = ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC;

    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_VOID_DESC = "()V";
    private static final String OBJECT_DESC = "Ljava/lang/Object;";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC = "()" + OBJECT_DESC;

    private static final Type acceptsNothingAndReturnsObjectType = getType(ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC);
    private static final String dependencyProviderName = getInternalName(DependencyProvider.class);
    private static final String dependencyProviderDesc = getDescriptor(DependencyProvider.class);
    private static final Handle metaFactoryHandle = new Handle(H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            false);

    private final String proxyName, proxyDesc, superName;
    private final String[] interfaces;
    private final List<Method> bindableMethods;

    ProxyClassVisitor(final ClassVisitor cv,
                      final String proxyName,
                      final Class<?> superclass,
                      final Class<?>[] interfaces,
                      final List<Method> bindableMethods) {
        super(ASM9, cv);
        this.proxyName = proxyName;
        this.proxyDesc = "L" + proxyName + ";";
        this.superName = getInternalName(superclass);
        int i = interfaces.length;
        this.interfaces = new String[i];
        while (0 <= --i) {
            this.interfaces[i] = getInternalName(interfaces[i]);
        }
        this.bindableMethods = bindableMethods;
    }

    @Override
    public void visit(int version,
                      int access,
                      String name,
                      String signature,
                      String superName,
                      String[] interfaces) {
        cv.visit(max(version, V1_8),
                access & ~ACC_ABSTRACT_INTERFACE | ACC_FINAL_SUPER_SYNTHETIC,
                this.proxyName,
                null,
                this.superName,
                this.interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return null;
    }

    @Override
    public void visitNestHost(String nestHost) {
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitNestMember(String nestMember) {
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
        for (final Method method : bindableMethods) {
            if (0 != (method.getModifiers() & ACC_ABSTRACT)) {
                continue;
            }
            final String methodName = method.getName();
            final String shimName = methodName + "$shim";
            final String methodDesc = getMethodDescriptor(method);
            final Type methodType = getType(methodDesc);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInvokeDynamicInsn(
                    "get",
                    "(" + proxyDesc + ")" + dependencyProviderDesc,
                    metaFactoryHandle,
                    acceptsNothingAndReturnsObjectType,
                    new Handle(H_INVOKESPECIAL, proxyName, shimName, methodDesc, false),
                    methodType
            );
            mv.visitFieldInsn(PUTFIELD, proxyName, proxyFieldName(method), dependencyProviderDesc);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void insertMethods() {
        for (final Method method : bindableMethods) {
            new Object() {

                final int access = method.getModifiers() & ~ACC_ABSTRACT_NATIVE | ACC_SYNTHETIC;
                final String methodName = method.getName();
                final String shimName = methodName + "$shim";
                final String fieldName = proxyFieldName(method);
                final String methodDesc = getMethodDescriptor(method);

                final Class<?> ownerClass = method.getDeclaringClass();
                final String ownerName = getInternalName(ownerClass);
                final boolean ownerIsIface = ownerClass.isInterface();

                final Class<?> returnType = method.getReturnType();
                final String returnTypeName = getInternalName(returnType);
                final String returnTypeDesc = getDescriptor(returnType);

                final Class<?> boxedReturnType = boxed(returnType);
                final String boxedReturnTypeName = getInternalName(boxedReturnType);
                final String boxedReturnTypeDesc = getDescriptor(boxedReturnType);

                final int returnOpCode = returnOpCode(method);

                {
                    generateProxyField();
                    generateProxyMethod();
                    generateShimMethod();
                }

                void generateProxyField() {
                    cv.visitField(ACC_PRIVATE_SYNTHETIC, fieldName, dependencyProviderDesc, null, null)
                            .visitEnd();
                }

                void generateProxyMethod() {
                    final MethodVisitor mv = beginMethod(methodName);
                    mv.visitFieldInsn(GETFIELD, proxyName, fieldName, dependencyProviderDesc);
                    mv.visitMethodInsn(INVOKEINTERFACE, dependencyProviderName, "get", ACCEPTS_NOTHING_AND_RETURNS_OBJECT_DESC, true);
                    if (!boxedReturnType.isAssignableFrom(Object.class)) {
                        mv.visitTypeInsn(CHECKCAST,
                                boxedReturnType.isArray() ? boxedReturnTypeDesc : boxedReturnTypeName);
                    }
                    if (boxedReturnType != returnType) {
                        assert !returnType.isArray();
                        mv.visitMethodInsn(INVOKEVIRTUAL,
                                boxedReturnTypeName,
                                returnTypeName + "Value",
                                "()" + returnTypeDesc,
                                false);
                    }
                    endMethod(mv);
                }

                void generateShimMethod() {
                    final MethodVisitor mv = beginMethod(shimName);
                    mv.visitMethodInsn(INVOKESPECIAL, ownerName, methodName, methodDesc, ownerIsIface);
                    endMethod(mv);
                }

                MethodVisitor beginMethod(final String methodName) {
                    final MethodVisitor mv = cv.visitMethod(access, methodName, methodDesc, null, null);
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    return mv;
                }

                void endMethod(final MethodVisitor mv) {
                    mv.visitInsn(returnOpCode);
                    mv.visitMaxs(-1, -1);
                    mv.visitEnd();
                }
            };
        }
    }

    private static String proxyFieldName(Method method) {
        return ((MethodInfo) () -> method).proxyFieldName();
    }

    private static int returnOpCode(final Method method) {
        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if (returnType == Void.TYPE) {
                throw new BreedingException("Method has void return type: " + method);
            } else if (returnType == Double.TYPE) {
                return DRETURN;
            } else if (returnType == Float.TYPE) {
                return FRETURN;
            } else if (returnType == Long.TYPE) {
                return LRETURN;
            } else {
                return IRETURN;
            }
        } else {
            if (returnType == Void.class) {
                throw new BreedingException("Method has Void return type: " + method);
            } else {
                return ARETURN;
            }
        }
    }
}
