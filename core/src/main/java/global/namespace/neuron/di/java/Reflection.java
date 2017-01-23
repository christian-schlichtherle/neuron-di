package global.namespace.neuron.di.java;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

class Reflection {

    private Reflection() { }

    static Find find(final String member) {
        return new Find() {
            @Override
            public Optional<MethodHandle> in(final Class<?> c) {
                try {
                    final Method method = c.getDeclaredMethod(member);
                    method.setAccessible(true);
                    return Optional.of(publicLookup()
                            .unreflect(method)
                            .asType(methodType(Object.class, c)));
                } catch (final NoSuchMethodException ignored) {
                    Optional<MethodHandle> result;
                    for (final Class<?> iface : c.getInterfaces()) {
                        if ((result = in(iface)).isPresent()) {
                            return result;
                        }
                    }
                    final Class<?> zuper = c.getSuperclass();
                    if (null != zuper) {
                        if ((result = in(zuper)).isPresent()) {
                            return result;
                        }
                    }
                    return Optional.empty();
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        };
    }

    interface Find {

        default Optional<MethodHandle> in(Object o) { return in(o.getClass()).map(mh -> mh.bindTo(o)); }

        Optional<MethodHandle> in(Class<?> c);
    }
}
