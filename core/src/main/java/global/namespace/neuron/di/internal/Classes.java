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

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static global.namespace.neuron.di.internal.Reflection.defineSubtype;
import static global.namespace.neuron.di.internal.Reflection.traverse;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

class Classes {

    private static final int ACC_PUBLIC_PRIVATE_PROTECTED =
            ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC;
    private static final int ACC_SUPER_ABSTRACT_SYNTHETIC =
            ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC;
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ACCEPTS_NOTHING_RETURNS_VOID = "()V";
    private static final String JAVA_LANG_OBJECT_FILE =
            internalName(Object.class.getName());
    private static final String IMPLEMENTED_BY_NEURON_DI = "$$ImplementedByNeuronDI";

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
        cw.visit(V1_8, ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers, internalName(implName), null, JAVA_LANG_OBJECT_FILE, new String[]{ internalName(ifaceName) });
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

        return defineSubtype(ifaceClass, implName, cw.toByteArray());
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

        final String traitName = traitClass.getName();
        final String implName = traitName + IMPLEMENTED_BY_NEURON_DI;
        final int modifiers = ACC_PUBLIC_PRIVATE_PROTECTED & traitClass.getModifiers();

        final ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_8, ACC_SUPER_ABSTRACT_SYNTHETIC | modifiers, internalName(implName), null, JAVA_LANG_OBJECT_FILE, new String[]{ internalName(traitName) });

        {
            traits.forEach(trait -> trait.nonAbstractMembers().forEach((declaration, implementation) -> {
                final MethodVisitor mv = cw.visitMethod(ACC_SYNTHETIC | ~ACC_ABSTRACT & declaration.getModifiers(), declaration.getName(), getMethodDescriptor(declaration), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESTATIC, trait.internalClassName(), implementation.getName(), getMethodDescriptor(implementation), false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }));
        }

        {
            final MethodVisitor mv = cw.visitMethod(ACC_SYNTHETIC | modifiers, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_RETURNS_VOID, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, JAVA_LANG_OBJECT_FILE, CONSTRUCTOR_NAME, ACCEPTS_NOTHING_RETURNS_VOID, false);
            for (final Trait trait : traits) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESTATIC, trait.internalClassName(), "$init$", "(L" + trait.internalIfaceName() + ";)V", false);
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        cw.visitEnd();

        return defineSubtype(traitClass, implName, cw.toByteArray());
    }

    private static String internalName(String className) {
        return className.replace('.', '/');
    }

    private interface Trait {

        default Map<Method, Method> nonAbstractMembers() {
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
                        } catch (NoSuchMethodException e) {
                            throw new AssertionError(e);
                        }
                    },
                    Map::putAll
            );
        }

        default String internalIfaceName() {
            return internalName(ifaceName());
        }

        default String ifaceName() { return ifaceClass().getName(); }

        Class<?> ifaceClass();

        default String internalClassName() {
            return internalName(className());
        }

        default String className() {  return classClass().getName(); }

        Class<?> classClass();
    }
}
