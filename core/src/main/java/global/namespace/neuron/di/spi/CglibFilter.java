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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

final class CglibFilter implements CallbackFilter {

    private final ArrayList<Method> methods;

    CglibFilter(final Class<?> superclass, final Class<?> interfaces[]) {
        methods = new ArrayList<>();
        Enhancer.getMethods(superclass, interfaces, methods);
        methods.trimToSize();
    }

    Callback[] callbacks(final CglibContext<?> ctx) {
        return new Visitor() {

            final Callback[] callbacks = new Callback[methods.size()];

            int index;

            @Override
            public void visitSynapse(final SynapseElement element) {
                final Supplier<?> resolve = ctx.supplier(element.method());
                callbacks[index] = element.synapseCallback(resolve::get);
            }

            @Override
            public void visitMethod(MethodElement element) {
                callbacks[index] = element.methodCallback();
            }

            Callback[] callbacks() {
                for (final Method method : methods) {
                    ctx.element(method).accept(this);
                    index++;
                }
                return callbacks;
            }
        }.callbacks();
    }

    @Override
    public int accept(Method method) {
        // This method is called at most once per runtime class and method,
        // so we can easily afford the overall runtime complexity of O(n*n),
        // where n is the number of methods, for the benefit of reduced
        // overhead when iterating through the methods.
        return methods.indexOf(method);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CglibFilter)) return false;
        final CglibFilter that = (CglibFilter) obj;
        return this.methods.equals(that.methods);
    }

    @Override
    public int hashCode() { return methods.hashCode(); }
}
