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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.*;

// The error checks in this class must match the error checks in
// `global.namespace.neuron.di.scala.NeuronAnnotation`!
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("global.namespace.neuron.di.java.Neuron")
public final class NeuronProcessor extends CommonProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // Call the `validate` method only for those elements where any of the annotations are DIRECTLY present:
        annotations.forEach(annotation ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .filter(element -> element
                                .getAnnotationMirrors()
                                .stream()
                                .anyMatch(mirror -> annotation.equals(mirror.getAnnotationType().asElement())))
                        .forEach(element -> validate((TypeElement) element)));
        return true;
    }

    private void validate(final TypeElement type) {
        if (!hasStaticContext(type)) {
            error("A neuron class must have a static context.", type);
        }
        if (type.getModifiers().contains(FINAL)) {
            error("A neuron class must not be final.", type);
        }
        if (!hasEitherNoConstructorOrANonPrivateConstructorWithoutParameters(type)) {
            error("A neuron type must have either no constructor or a non-private constructor without parameters.", type);
        }
        if (isSerializable(type)) {
            warn("A neuron type should not be serializable.", type);
        }
    }

    private static boolean hasStaticContext(TypeElement type) {
        return !type.getNestingKind().isNested() || type.getModifiers().contains(STATIC);
    }

    private static boolean hasEitherNoConstructorOrANonPrivateConstructorWithoutParameters(TypeElement type) {
        final List<? extends Element> constructors = type.getEnclosedElements().stream()
                .filter(elem -> elem.getKind() == CONSTRUCTOR)
                .collect(Collectors.toList());
        return constructors.isEmpty() || constructors.stream()
                .anyMatch(elem -> isNonPrivateConstructorWithoutParameters((ExecutableElement) elem));
    }

    private static boolean isNonPrivateConstructorWithoutParameters(ExecutableElement ctor) {
        return !ctor.getModifiers().contains(PRIVATE) && ctor.getParameters().isEmpty();
    }

    private static boolean isSerializable(TypeElement type) {
        return type.getInterfaces().stream().anyMatch(i -> i.toString().equals("java.io.Serializable"));
    }
}
