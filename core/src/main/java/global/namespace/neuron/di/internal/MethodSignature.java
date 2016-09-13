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

import static org.objectweb.asm.Type.getMethodDescriptor;

final class MethodSignature {

    private final String name, descriptor;

    MethodSignature(Method method) {
        this(method.getName(), getMethodDescriptor(method));
    }

    MethodSignature(final String name, final String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MethodSignature)) return false;
        final MethodSignature that = (MethodSignature) obj;
        return this.name.equals(that.name) &&
                this.descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        int result = 17 + name.hashCode();
        result = 31 * result + descriptor.hashCode();
        return result;
    }
}
