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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

abstract class CommonProcessor extends AbstractProcessor {

    void error(CharSequence message, javax.lang.model.element.Element e) {
        messager().printMessage(ERROR, message , e);
    }

    void warning(CharSequence message, javax.lang.model.element.Element e) {
        messager().printMessage(WARNING, message , e);
    }

    private Messager messager() { return processingEnv.getMessager(); }

    Elements elementUtils() { return processingEnv.getElementUtils(); }
}
