package global.tranquillity.neuron.di.api;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Defines a domain specific language (DSL) for configuring generic modules and
 * dependency injectors.
 * Implementations may extend the DSL in order to provide full support for a
 * specific dependency injection (DI) framework, e.g. Google Guice.
 *
 * @param <Injector> the type of the injectors.
 * @param <Module> the type of the modules.
 * @author Christian Schlichtherle
 */
public abstract class BindingContext<Injector, Module> {

    public abstract InjectorBuilder injector();

    public abstract class InjectorBuilder
            extends ModuleContainer<InjectorBuilder>
            implements Builder<Injector> { }

    public abstract class ModuleContainer<This extends ModuleContainer<This>> {

        public abstract ModuleBuilder<This> module();

        public abstract This module(Module module);
    }

    public abstract class ModuleBuilder<Parent>
            extends ModuleContainer<ModuleBuilder<Parent>>
            implements Builder<Module>, Injection<Parent> {

    }

    public interface AnnotatedBindingBuilderWithInjection<Type, Parent>
            extends LinkedBindingBuilderWithInjection<Type, Parent> {

        LinkedBindingBuilderWithInjection<Type, Parent> annotatedWith(Class<? extends Annotation> annotationType);

        LinkedBindingBuilderWithInjection<Type, Parent> annotatedWith(Annotation annotation);
    }

    public interface LinkedBindingBuilderWithInjection<Type, Parent>
            extends ScopedBindingBuilderWithInjection<Parent> {

        ScopedBindingBuilderWithInjection<Parent> to(Class<? extends Type> implementation);

        Injection<Parent> toInstance(Type instance);

        ScopedBindingBuilderWithInjection<Parent> toProvider(Provider<? extends Type> provider);

        ScopedBindingBuilderWithInjection<Parent> toProvider(Class<? extends Provider<? extends Type>> providerType);

        <S extends Type> ScopedBindingBuilderWithInjection<Parent> toConstructor(Constructor<S> constructor);
    }

    public interface ScopedBindingBuilderWithInjection<Parent>
            extends Injection<Parent> {

        Injection<Parent> in(Class<? extends Annotation> scopeAnnotation);

        Injection<Parent> asEagerSingleton();
    }

    public interface AnnotatedElementBuilderWithInjection<Parent>
            extends Injection<Parent> {

        Injection<Parent> annotatedWith(Class<? extends Annotation> annotationType);

        Injection<Parent> annotatedWith(Annotation annotation);
    }

    public interface AnnotatedConstantBindingBuilderWithInjection<Parent>
            extends Injection<Parent> {

        ConstantBindingBuilderWithInjection<Parent> annotatedWith(Class<? extends Annotation> annotationType);

        ConstantBindingBuilderWithInjection<Parent> annotatedWith(Annotation annotation);
    }

    public interface ConstantBindingBuilderWithInjection<Parent>
            extends Injection<Parent> {

        Injection<Parent> to(String value);

        Injection<Parent> to(int value);

        Injection<Parent> to(long value);

        Injection<Parent> to(boolean value);

        Injection<Parent> to(double value);

        Injection<Parent> to(float value);

        Injection<Parent> to(short value);

        Injection<Parent> to(char value);

        Injection<Parent> to(byte value);

        Injection<Parent> to(Class<?> value);

        <E extends Enum<E>> Injection<Parent> to(E value);
    }

    /**
     * A builder for some product.
     * Builders are stateful and thus not thread-safe.
     *
     * @param <Product> the type of the product.
     */
    public interface Builder<Product> {

        /** Builds and returns a new product. */
        Product build();
    }

    /**
     * Injects a dependency into some target.
     *
     * @param <Target> the type of the target.
     */
    public interface Injection<Target> {

        /** Injects a dependency into the target and returns the target. */
        Target inject();
    }
}
