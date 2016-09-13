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

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static global.namespace.neuron.di.internal.Reflection.approximateClassLoader;
import static global.namespace.neuron.di.internal.Reflection.defineSubclass;
import static global.namespace.neuron.di.internal.Reflection.traverse;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

class Classes {

    private static final int ACC_PUBLIC_PRIVATE_PROTECTED =
            ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC;
    private static final int ACC_SUPER_ABSTRACT_SYNTHETIC =
            ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC;
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_AND_RETURNS_VOID = "()V";
    private static final String JAVA_LANG_OBJECT =
            getInternalName(Object.class);
    private static final String IMPLEMENTED_BY_NEURON_DI =
            "$$ImplementedByNeuronDI";

    private Classes() { }

    /**
     * Returns a class which implements the given Java interface.
     * The class implements any default methods of the interface.
     */
    static <T> Class<? extends T> classImplementingJava(final Class<T> ifaceClass) {
        if (!ifaceClass.isInterface()) {
            throw new IllegalArgumentException();
        }

        final String ifaceName = ifaceClass.getName();
        final String implName = ifaceName + IMPLEMENTED_BY_NEURON_DI;
        final int modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & ifaceClass.getModifiers();

        final ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_8, ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers, internalName(implName), null, JAVA_LANG_OBJECT, new String[]{ internalName(ifaceName) });

        final MethodVisitor mv = cw.visitMethod(modifiers, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_AND_RETURNS_VOID, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_LANG_OBJECT, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_AND_RETURNS_VOID, false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        return defineSubclass(ifaceClass, implName, cw.toByteArray());
    }

    /**
     * Returns a class which implements the given Scala trait.
     * The class implements any non-abstract members of the trait.
     */
    static <T> Class<? extends T> classImplementingScala(final Class<T> traitClass) {
        if (!traitClass.isInterface()) {
            throw new IllegalArgumentException();
        }

        final Deque<Trait> traits = new LinkedList<>();
        traverse(ifaceClass -> {
            final ClassLoader cl = ifaceClass.getClassLoader();
            if (null != cl) {
                try {
                    final Class<?> classClass = cl.loadClass(ifaceClass.getName() + "$class");
                    traits.addFirst(new Trait() {

                        @Override
                        public Class<?> ifaceClass() { return ifaceClass; }

                        @Override
                        public Class<?> classClass() { return classClass; }
                    });
                } catch (ClassNotFoundException ignored) {
                }
            }
        }).accept(traitClass);
        final Map<Method, Method> methods = traits
                .stream()
                .flatMap(trait -> trait.declaredNonAbstractMembers().entrySet().stream())
                .collect(
                        LinkedHashMap::new,
                        (map, entry) -> map.putIfAbsent(entry.getKey(), entry.getValue()),
                        Map::putAll
                );

        final String traitName = traitClass.getName();
        final String implName = traitName + IMPLEMENTED_BY_NEURON_DI;
        final int modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & traitClass.getModifiers();

        final ClassReader cr = classReader(traitClass);
        final ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(ASM5, cw) {

            @Override
            public void visit(final int version,
                              final int access,
                              final String name,
                              final String signature,
                              final String superName,
                              final String[] interfaces) {
                cv.visit(version,
                        ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers,
                        internalName(implName),
                        signature,
                        JAVA_LANG_OBJECT,
                        new String[]{ internalName(traitName) });
            }

            @Override
            public MethodVisitor visitMethod(final int access,
                                             final String name,
                                             final String desc,
                                             final String signature,
                                             final String[] exceptions) {
                if (methods.keySet()
                        .stream()
                        .filter(declaration -> name.equals(declaration.getName()))
                        .anyMatch(declaration -> desc.equals(getMethodDescriptor(declaration)))) {
                    return null;
                } else {
                    return cv.visitMethod(access, name, desc, signature, exceptions);
                }
            }

            @Override
            public void visitEnd() {
                final ClassVisitor target = cv;
                methods.forEach((declaration, implementation) -> {
                    final ClassReader cr = classReader(declaration.getDeclaringClass());
                    cr.accept(new ClassVisitor(ASM5) {

                        @Override
                        public MethodVisitor visitMethod(final int access,
                                                         final String name,
                                                         final String desc,
                                                         final String signature,
                                                         final String[] exceptions) {
                            if (name.equals(declaration.getName()) &&
                                    desc.equals(getMethodDescriptor(declaration))) {
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

                insertConstructor();

                target.visitEnd();
            }

            void insertConstructor() {
                final MethodVisitor mv = cv.visitMethod(
                        ACC_SYNTHETIC | modifiers,
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
                for (final Trait trait : traits) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC,
                            trait.internalClassName(),
                            "$init$",
                            "(L" + trait.internalIfaceName() + ";)V",
                            false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
        }, SKIP_DEBUG);

        return defineSubclass(traitClass, implName, cw.toByteArray());
    }

    private static <T> ClassReader classReader(final Class<T> clazz) {
        try (InputStream in = approximateClassLoader(clazz)
                .getResourceAsStream(getInternalName(clazz) + ".class")) {
            return new ClassReader(in);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String internalName(String className) {
        return className.replace('.', '/');
    }

    private interface Trait {

        default Map<Method, Method> declaredNonAbstractMembers() {
            return Stream.of(classClass().getDeclaredMethods()).collect(
                    LinkedHashMap::new,
                    (map, method) -> {
                        final String name = method.getName();
                        if ("$init$".equals(name)) {
                            return;
                        }
                        final List<Class<?>> parameterTypes =
                                new ArrayList<>(Arrays.asList(method.getParameterTypes()));
                        parameterTypes.remove(0);
                        try {
                            map.put(ifaceClass().getDeclaredMethod(
                                    name,
                                    parameterTypes.toArray(new Class<?>[parameterTypes.size()])),
                                    method);
                        } catch (NoSuchMethodException ignored) {
                            // This may happen if the implementation method has
                            // the name "$init$" or access modifiers private,
                            // final etc.
                        }
                    },
                    Map::putAll
            );
        }

        default String internalIfaceName() { return internalName(ifaceName()); }

        default String ifaceName() { return ifaceClass().getName(); }

        Class<?> ifaceClass();

        default String internalClassName() { return internalName(className()); }

        default String className() {  return classClass().getName(); }

        Class<?> classClass();
    }
}
