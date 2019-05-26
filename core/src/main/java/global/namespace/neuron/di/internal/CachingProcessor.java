/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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

import global.namespace.neuron.di.java.Neuron;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.VOID;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("global.namespace.neuron.di.java.Caching")
public final class CachingProcessor extends CommonProcessor {

    private static final String GLOBAL_NAMESPACE_NEURON_DI_JAVA_NEURON = Neuron.class.getName();
    private static final String JAVA_LANG_VOID = Void.class.getName();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation).forEach(this::validateElement));
        return true;
    }

    private void validateElement(final Element element) {
        if (element instanceof ExecutableElement) {
            validateMethod((ExecutableElement) element);
        }
    }

    private void validateMethod(final ExecutableElement element) {
        final Element type = element.getEnclosingElement();
        if (!hasNeuronAnnotation(type)) {
            error("A caching method must be a member of a neuron type...", element);
            error("... but there is no @Neuron annotation here.", type);
        }
        final Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(STATIC)) {
            error("A caching method must not be static.", element);
        }
        if (modifiers.contains(FINAL)) {
            error("A caching method must not be final.", element);
        }
        if (modifiers.contains(PRIVATE)) {
            error("A caching method must not be private.", element);
        }
        if (!element.getParameters().isEmpty()) {
            error("A caching method must not have parameters.", element);
        }
        if (isVoid(element.getReturnType())) {
            error("A caching method must have a return value.", element);
        }
    }

    private boolean hasNeuronAnnotation(final Element element) {
        return new Object() {

            final Set<Element> visited = new HashSet<>();

            boolean check(final Element e) {
                if (visited.add(e)) {
                    for (final AnnotationMirror m : getAllAnnotationMirrors(e)) {
                        final DeclaredType t = m.getAnnotationType();
                        if (t.toString().equals(GLOBAL_NAMESPACE_NEURON_DI_JAVA_NEURON) || check(t.asElement())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }.check(element);
    }

    private List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
        return elements().getAllAnnotationMirrors(e);
    }

    private Elements elements() {
        return processingEnv.getElementUtils();
    }

    private static boolean isVoid(TypeMirror type) {
        return type.getKind() == VOID || type.toString().equals(JAVA_LANG_VOID);
    }
}
