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

import net.sf.cglib.proxy.*;

import java.util.function.Supplier;

final class CglibHack<T> {

    private static final FixedValue INVALID_FIXED_VALUE =
            () -> { throw new AssertionError(); };

    private static final MethodInterceptor INVALID_METHOD_INTERCEPTOR =
            (obj, method, args, proxy) -> { throw new AssertionError(); };

    private CglibFilter filter;
    private Factory factory;
    private Callback[] callbacks;

    CglibHack(final CglibContext<T> ctx) {
        new CglibFunction<>((superclass, interfaces) -> {
            filter = new CglibFilter(superclass, interfaces);
            final Enhancer e = new Enhancer();
            e.setSuperclass(superclass);
            e.setInterfaces(interfaces);
            e.setCallbackFilter(filter);
            e.setCallbacks(invalidate(callbacks = callbacks(ctx)));
            e.setNamingPolicy(NeuronDINamingPolicy.SINGLETON);
            e.setUseCache(false);
            factory = (Factory) e.create();
            return null;
        }).apply(ctx.runtimeClass());
    }

    private static Callback[] invalidate(final Callback[] callbacks) {
        final Callback[] results = callbacks.clone();
        for (int i = results.length; --i >= 0; ) {
            final Callback result = results[i];
            if (result instanceof FixedValue) {
                results[i] = INVALID_FIXED_VALUE;
            } else if (result instanceof MethodInterceptor) {
                results[i] = INVALID_METHOD_INTERCEPTOR;
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    T newInstance(CglibContext<T> ctx) {
        return (T) factory.newInstance(callbacks(ctx));
    }

    private Callback[] callbacks(CglibContext<T> ctx) {
        return callbacksSupplier(ctx).get();
    }

    private synchronized Supplier<Callback[]> callbacksSupplier(
            final CglibContext<T> ctx) {
        if (null != callbacks) {
            final Callback[] c = callbacks;
            callbacks = null;
            return () -> c;
        } else {
            return () -> filter.callbacks(ctx);
        }
    }
}

