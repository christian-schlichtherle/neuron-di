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

import global.namespace.neuron.di.java.Neuron;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.VOID;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("global.namespace.neuron.di.java.Caching")
public final class CachingProcessor extends CommonProcessor {

    private static final String JAVA_LANG_VOID = Void.class.getName();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .forEach(elem -> validateMethod((ExecutableElement) elem)));
        return true;
    }

    private void validateMethod(final ExecutableElement method) {
        final javax.lang.model.element.Element enclElem =
                method.getEnclosingElement();
        if (null == enclElem.getAnnotation(Neuron.class)) {
            error("A caching method must be a member of a neuron class or interface, but...", method);
            error("... there is no @Neuron annotation here.", enclElem);
        }
        final Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(FINAL)) {
            error("A caching method must not be final.", method);
        }
        if (modifiers.contains(PRIVATE)) {
            error("A caching method must not be private.", method);
        }
        if (modifiers.contains(STATIC)) {
            error("A caching method must not be static.", method);
        }
        if (!method.getParameters().isEmpty()) {
            error("A caching method must not have parameters.", method);
        }
        if (isVoid(method.getReturnType())) {
            error("A caching method must not have a void return type.", method);
        }
    }

    private static boolean isVoid(TypeMirror type) {
        return type.getKind() == VOID || type.toString().equals(JAVA_LANG_VOID);
    }
}
