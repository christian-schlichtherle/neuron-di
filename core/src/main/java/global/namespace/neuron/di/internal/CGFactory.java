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

import net.sf.cglib.proxy.*;

final class CGFactory {

    private static final MethodInterceptor INVALID_METHOD_INTERCEPTOR =
            (obj, method, args, proxy) -> { throw new AssertionError(); };

    private static final FixedValue INVALID_FIXED_VALUE =
            () -> { throw new AssertionError(); };

    private CGCallbackFilter filter;
    private Factory factory;

    CGFactory(final CGContext ctx) {
        new ClassAdapter((superclass, interfaces) -> {
            filter = new CGCallbackFilter(superclass, interfaces);
            final Enhancer e = new Enhancer();
            e.setSuperclass(superclass);
            e.setInterfaces(interfaces);
            e.setCallbackFilter(filter);
            e.setCallbacks(invalidate(callbacks(ctx)));
            e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
            e.setUseCache(false);
            factory = (Factory) e.create();
        }).accept(ctx.runtimeClass());
    }

    private static Callback[] invalidate(final Callback[] callbacks) {
        final Callback[] results = callbacks.clone();
        for (int i = results.length; --i >= 0; ) {
            final Callback result = results[i];
            if (result instanceof MethodInterceptor) {
                results[i] = INVALID_METHOD_INTERCEPTOR;
            } else if (result instanceof FixedValue) {
                results[i] = INVALID_FIXED_VALUE;
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    Object newInstance(CGContext ctx) {
        return factory.newInstance(callbacks(ctx));
    }

    private Callback[] callbacks(CGContext ctx) {
        return ctx.callbacks(filter);
    }
}
