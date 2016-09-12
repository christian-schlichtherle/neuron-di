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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

interface ASM {

    static Class<?> interface2class(final Class<?> iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException();
        }
        final Enhancer e = new Enhancer();
        e.setSuperclass(Object.class);
        e.setInterfaces(new Class<?>[] { iface });
        e.setCallbackType(NoOp.class);
        e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
        e.setUseCache(false);
        e.setUseFactory(false);
        return e.createClass();
    }
}
