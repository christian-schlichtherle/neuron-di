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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class MethodProxyFilter {

    private final List<Method> methods;

    static MethodProxyFilter from(final Class<?> superclass, final Class<?>[] interfaces) {
        final OverridableMethodsCollector c = new OverridableMethodsCollector().add(superclass);
        for (Class<?> i : interfaces) {
            c.add(i);
        }
        return new MethodProxyFilter(c.methods.values());
    }

    private MethodProxyFilter(final Collection<Method> methods) { this.methods = new ArrayList<>(methods); }

    List<Method> proxiedMethods(final NeuronProxyContext ctx) {
        return new Visitor() {

            final ArrayList<Method> filtered = new ArrayList<>(methods.size());

            public void visitSynapse(final SynapseElement element) { filtered.add(element.method()); }

            public void visitMethod(final MethodElement element) {
                if (element.isCachingEnabled()) {
                    filtered.add(element.method());
                }
            }

            List<Method> proxiedMethods() {
                for (Method method : methods) {
                    ctx.element(method).accept(this);
                }
                filtered.trimToSize();
                return filtered;
            }
        }.proxiedMethods();
    }
}
