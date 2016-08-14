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
public abstract class InjectorContext<Injector, Module> {

    public abstract InjectorBuilder injector();

    public abstract class InjectorBuilder
            extends ModuleContext<InjectorBuilder>
            implements Builder<Injector> { }

    public abstract class ModuleContext<This extends ModuleContext<This>> {

        public abstract ModuleBuilder<? extends This> module();

        public abstract This module(Module module);
    }

    public abstract class ModuleBuilder<Parent>
            extends ModuleContext<ModuleBuilder<Parent>>
            implements Builder<Module>, Definition<Parent> {

        public abstract <Type> AnnotatedBindingDefinition<Type, ? extends ModuleBuilder<Parent>> exposeAndBind(Class<Type> clazz);

        public abstract AnnotatedElementDefinition<? extends ModuleBuilder<Parent>> expose(Class<?> clazz);

        public abstract <Type> AnnotatedBindingDefinition<Type, ? extends ModuleBuilder<Parent>> bind(Class<Type> clazz);

        public abstract AnnotatedConstantBindingDefinition<? extends ModuleBuilder<Parent>> bindConstant();
    }

    public interface AnnotatedBindingDefinition<Type, Parent>
            extends LinkedBindingDefinition<Type, Parent> {

        LinkedBindingDefinition<Type, Parent> annotatedWith(Class<? extends Annotation> annotationType);

        LinkedBindingDefinition<Type, Parent> annotatedWith(Annotation annotation);
    }

    public interface LinkedBindingDefinition<Type, Parent>
            extends ScopedBindingDefinition<Parent> {

        ScopedBindingDefinition<Parent> to(Class<? extends Type> implementation);

        Definition<Parent> toInstance(Type instance);

        ScopedBindingDefinition<Parent> toProvider(Provider<? extends Type> provider);

        ScopedBindingDefinition<Parent> toProvider(Class<? extends Provider<? extends Type>> providerType);

        <S extends Type> ScopedBindingDefinition<Parent> toConstructor(Constructor<S> constructor);
    }

    public interface ScopedBindingDefinition<Parent>
            extends Definition<Parent> {

        Definition<Parent> in(Class<? extends Annotation> scopeAnnotation);

        Definition<Parent> asEagerSingleton();
    }

    public interface AnnotatedElementDefinition<Parent>
            extends Definition<Parent> {

        Definition<Parent> annotatedWith(Class<? extends Annotation> annotationType);

        Definition<Parent> annotatedWith(Annotation annotation);
    }

    public interface AnnotatedConstantBindingDefinition<Parent>
            extends Definition<Parent> {

        ConstantBindingDefinition<Parent> annotatedWith(Class<? extends Annotation> annotationType);

        ConstantBindingDefinition<Parent> annotatedWith(Annotation annotation);
    }

    public interface ConstantBindingDefinition<Parent>
            extends Definition<Parent> {

        Definition<Parent> to(String value);

        Definition<Parent> to(int value);

        Definition<Parent> to(long value);

        Definition<Parent> to(boolean value);

        Definition<Parent> to(double value);

        Definition<Parent> to(float value);

        Definition<Parent> to(short value);

        Definition<Parent> to(char value);

        Definition<Parent> to(byte value);

        Definition<Parent> to(Class<?> value);

        <E extends Enum<E>> Definition<Parent> to(E value);
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
     * Defines an injectable product.
     * Definitions are stateful and thus not thread-safe.
     *
     * @param <Target> the type of the injection target.
     */
    public interface Definition<Target> {

        /**
         * Builds the product, injects it into the target and returns the
         * target.
         */
        Target end();
    }
}
