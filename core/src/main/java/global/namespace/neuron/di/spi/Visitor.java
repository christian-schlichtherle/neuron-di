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

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

interface Visitor {

    default void visitNeuron(NeuronElement element) {
        new CGFunction<>((superclass, interfaces) -> {
            for (Method method : new CGFilter(superclass, interfaces)) {
                element.element(method).accept(this);
            }
            return null;
        }).apply(element.runtimeClass());
    }

    default void visitClass(ClassElement element) { }

    default void visitSynapse(SynapseElement element) { }

    default void visitMethod(MethodElement element) { }
}
