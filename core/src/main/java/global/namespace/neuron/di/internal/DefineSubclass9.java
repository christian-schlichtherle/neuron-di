package global.namespace.neuron.di.internal;

import global.namespace.neuron.di.internal.proxy.Proxies;
import global.namespace.neuron.di.java.BreedingException;

import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

final class DefineSubclass9 implements DefineSubclass {

    private static final MethodHandles.Lookup lookup = lookup();

    @Override
    @SuppressWarnings({"unchecked", "Since15"})
    public <C> Class<? extends C> apply(final Class<C> clazz, final String name, final byte[] b) {
        try {
            return (Class<? extends C>) privateLookupIn(null != clazz.getClassLoader() ? clazz : Proxies.class, lookup)
                    .defineClass(b);
        } catch (IllegalAccessException e) {
            throw new BreedingException(e);
        }
    }
}
