package global.namespace.neuron.di.java;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

class Reflection {

    private Reflection() { }

    static Find find(final String member) {
        return o -> new Function<Class<?>, Optional<MethodHandle>>() {
            @Override
            public Optional<MethodHandle> apply(final Class<?> c) {
                try {
                    return methodHandle(c.getDeclaredMethod(member), publicLookup()::unreflect);
                } catch (final NoSuchMethodException ignored) {
                    try {
                        return methodHandle(c.getDeclaredField(member), publicLookup()::unreflectGetter);
                    } catch (final NoSuchFieldException ignoredAgain) {
                        Optional<MethodHandle> result;
                        for (final Class<?> iface : c.getInterfaces()) {
                            if ((result = apply(iface)).isPresent()) {
                                return result;
                            }
                        }
                        final Class<?> zuper = c.getSuperclass();
                        if (null != zuper) {
                            if ((result = apply(zuper)).isPresent()) {
                                return result;
                            }
                        }
                        return Optional.empty();
                    }
                }
            }

            <M extends AccessibleObject & Member>
            Optional<MethodHandle> methodHandle(M member, Unreflect<M> unreflect) {
                member.setAccessible(true);
                MethodHandle mh;
                try {
                    mh = unreflect.apply(member);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
                if (0 == (member.getModifiers() & Modifier.STATIC)) {
                    mh = mh.bindTo(o);
                }
                return Optional.of(mh.asType(methodType(Object.class)));
            }
        }.apply(o.getClass());
    }

    private interface Unreflect<M> {

        MethodHandle apply(M member) throws IllegalAccessException;
    }

    interface Find {

        Optional<MethodHandle> in(Object o);
    }
}
