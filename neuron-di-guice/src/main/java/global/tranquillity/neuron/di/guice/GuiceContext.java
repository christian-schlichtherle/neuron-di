package global.tranquillity.neuron.di.guice;

import com.google.inject.*;
import com.google.inject.Scope;
import global.tranquillity.neuron.di.api.InjectorContext;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public abstract class GuiceContext extends InjectorContext<Injector, Module> {

    public abstract class InjectorBuilder4Guice extends InjectorBuilder {

        @Override
        public abstract ModuleBuilder4Guice<? extends InjectorBuilder4Guice> module();

        @Override
        public abstract InjectorBuilder4Guice module(Module module);
    }

    public abstract class ModuleBuilder4Guice<Parent>
            extends ModuleBuilder<Parent> {

        @Override
        public abstract ModuleBuilder4Guice<? extends ModuleBuilder4Guice<Parent>> module();

        @Override
        public abstract ModuleBuilder4Guice<Parent> module(Module module);

        @Override
        public abstract <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(Class<Type> clazz);

        public abstract <Type> LinkedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(Key<Type> key);

        public abstract <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(TypeLiteral<Type> literal);

        @Override
        public abstract AnnotatedElementDefinition<? extends ModuleBuilder4Guice<Parent>> expose(Class<?> clazz);

        public abstract Definition<? extends ModuleBuilder4Guice<Parent>> expose(Key<?> key);

        public abstract AnnotatedElementDefinition<? extends ModuleBuilder4Guice<Parent>> expose(TypeLiteral<?> literal);

        @Override
        public abstract <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(Class<Type> clazz);

        public abstract <Type> LinkedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(Key<Type> key);

        public abstract <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(TypeLiteral<Type> literal);

        @Override
        public abstract AnnotatedConstantBindingDefinition<? extends ModuleBuilder4Guice<Parent>> bindConstant();
    }

    public interface AnnotatedBindingDefinition4Guice<Type, Parent>
            extends LinkedBindingDefinition4Guice<Type, Parent>,
            AnnotatedBindingDefinition<Type, Parent> {

        @Override
        LinkedBindingDefinition4Guice<Type, Parent> annotatedWith(Class<? extends Annotation> annotationType);

        @Override
        LinkedBindingDefinition4Guice<Type, Parent> annotatedWith(Annotation annotation);
    }

    public interface LinkedBindingDefinition4Guice<Type, Parent>
            extends ScopedBindingDefinition4Guice<Parent>,
            LinkedBindingDefinition<Type, Parent> {

        @Override
        ScopedBindingDefinition4Guice<Parent> to(Class<? extends Type> implementation);

        ScopedBindingDefinition4Guice<Parent> to(TypeLiteral<? extends Type> implementation);

        ScopedBindingDefinition4Guice<Parent> to(Key<? extends Type> targetKey);

        @Override
        ScopedBindingDefinition4Guice<Parent> toProvider(Provider<? extends Type> provider);

        @Override
        ScopedBindingDefinition4Guice<Parent> toProvider(Class<? extends Provider<? extends Type>> providerType);

        ScopedBindingDefinition4Guice<Parent> toProvider(TypeLiteral<? extends Provider<? extends Type>> providerType);

        ScopedBindingDefinition4Guice<Parent> toProvider(Key<? extends Provider<? extends Type>> providerKey);

        @Override
        <S extends Type> ScopedBindingDefinition4Guice<Parent> toConstructor(Constructor<S> constructor);

        <S extends Type> ScopedBindingDefinition4Guice<Parent> toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type);
    }

    public interface ScopedBindingDefinition4Guice<Parent>
            extends ScopedBindingDefinition<Parent> {

        Definition<Parent> in(Scope scope);
    }
}
