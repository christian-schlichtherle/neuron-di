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
package global.namespace.neuron.di.spi;

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
@SupportedAnnotationTypes("global.namespace.neuron.di.api.Neuron")
public final class NeuronProcessor extends CommonProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .filter(elem -> elem.getKind().isClass())
                        .forEach(elem -> validateClass((TypeElement) elem)));
        return true;
    }

    private void validateClass(final TypeElement clazz) {
        final Set<Modifier> modifiers = clazz.getModifiers();
        if (!modifiers.contains(ABSTRACT) && !isRunWithNeuronJUnitRunner(clazz)) {
            warning("A neuron class should be abstract or annotated with @org.junit.runner.RunWith(global.namespace.neuron.di.api.junit.NeuronJUnitRunner.class).", clazz);
        }
        if (modifiers.contains(FINAL)) {
            error("A neuron class must not be final.", clazz);
        }
        if (clazz.getNestingKind().isNested()) {
            if (!modifiers.contains(STATIC)) {
                error("A neuron class must be static.", clazz);
            }
        }
        if (!hasNonPrivateConstructorWithoutParameters(clazz)) {
            error("A neuron class must have a non-private constructor without parameters.", clazz);
        }
    }

    private boolean isRunWithNeuronJUnitRunner(TypeElement clazz) {
        return elementUtils()
                .getAllAnnotationMirrors(clazz)
                .stream()
                .filter(mirror -> mirror.getAnnotationType().toString().equals("org.junit.runner.RunWith"))
                .flatMap(mirror -> mirror.getElementValues().entrySet().stream())
                .filter(entry -> entry.getKey().getSimpleName().toString().equals("value"))
                .anyMatch(entry -> entry.getValue().getValue().toString().equals("global.namespace.neuron.di.api.junit.NeuronJUnitRunner"));
    }

    private static boolean hasNonPrivateConstructorWithoutParameters(TypeElement type) {
        return type
                .getEnclosedElements()
                .stream()
                .filter(elem -> elem.getKind() == CONSTRUCTOR)
                .anyMatch(elem -> isNonPrivateConstructorWithoutParameters((ExecutableElement) elem));
    }

    private static boolean isNonPrivateConstructorWithoutParameters(ExecutableElement ctor) {
        return !ctor.getModifiers().contains(PRIVATE) &&
                ctor.getParameters().isEmpty();
    }
}
