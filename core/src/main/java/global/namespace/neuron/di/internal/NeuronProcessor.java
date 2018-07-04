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

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("global.namespace.neuron.di.java.Neuron")
public final class NeuronProcessor extends CommonProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .filter(element -> element
                                .getAnnotationMirrors()
                                .stream()
                                .anyMatch(mirror -> mirror.getAnnotationType().asElement() == annotation))
                        .forEach(element -> validate((TypeElement) element)));
        return true;
    }

    private void validate(final TypeElement type) {
        final Set<Modifier> modifiers = type.getModifiers();
        if (!hasStaticContext(type)) {
            error("A neuron class must have a static context.", type);
        }
        if (modifiers.contains(FINAL)) {
            error("A neuron class must not be final.", type);
        }
        if (!hasNonPrivateConstructorWithoutParameters(type)) {
            error("A neuron class must have a non-private constructor without parameters.", type);
        }
        if (isSerializable(type)) {
            error("A neuron type must not be serializable.", type);
        }
    }

    private static boolean hasStaticContext(TypeElement type) {
        return !type.getNestingKind().isNested() || type.getModifiers().contains(STATIC);
    }

    private static boolean hasNonPrivateConstructorWithoutParameters(TypeElement type) {
        return !type.getKind().isClass() || type.getEnclosedElements().stream()
                .filter(elem -> elem.getKind() == CONSTRUCTOR)
                .anyMatch(elem -> isNonPrivateConstructorWithoutParameters((ExecutableElement) elem));
    }

    private static boolean isNonPrivateConstructorWithoutParameters(ExecutableElement ctor) {
        return !ctor.getModifiers().contains(PRIVATE) && ctor.getParameters().isEmpty();
    }

    private static boolean isSerializable(TypeElement type) {
        return type.getInterfaces().stream().anyMatch(i -> i.toString().equals("java.io.Serializable"));
    }
}
