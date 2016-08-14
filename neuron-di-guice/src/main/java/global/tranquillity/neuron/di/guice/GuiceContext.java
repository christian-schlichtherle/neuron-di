package global.tranquillity.neuron.di.guice;

import com.google.inject.*;
import com.google.inject.binder.*;
import global.tranquillity.neuron.di.api.InjectorContext;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

public class GuiceContext extends InjectorContext<Injector, Module> {

    public InjectorBuilder4Guice injector() {
        return new InjectorBuilder4Guice();
    }

    private static <T> List<T> newList() { return new LinkedList<T>(); }

    public class InjectorBuilder4Guice extends InjectorBuilder {

        private List<Module> modules = newList();

        @Override
        public ModuleBuilder4Guice<? extends InjectorBuilder4Guice> module() {
            return new ModuleBuilder4Guice<InjectorBuilder4Guice>() {

                @Override
                public InjectorBuilder4Guice end() {
                    return InjectorBuilder4Guice.this.module(build());
                }
            };
        }

        @Override
        public InjectorBuilder4Guice module(Module module) {
            modules.add(module);
            return this;
        }

        @Override public Injector build() {
            return Guice.createInjector(swapModules());
        }

        private List<Module> swapModules() {
            try {
                return this.modules;
            } finally {
                this.modules = newList();
            }
        }
    }

    public abstract class ModuleBuilder4Guice<Parent> extends ModuleBuilder<Parent> {

        private List<Module> modules = newList();
        private List<Configuration<?>> exposings = newList();
        private List<Configuration<?>> bindings = newList();

        @Override
        public ModuleBuilder4Guice<? extends ModuleBuilder4Guice<Parent>> module() {
            return new ModuleBuilder4Guice<ModuleBuilder4Guice<Parent>>() {

                @Override
                public ModuleBuilder4Guice<Parent> end() {
                    return ModuleBuilder4Guice.this.module(build());
                }
            };
        }

        @Override
        public ModuleBuilder4Guice<Parent> module(Module module) {
            modules.add(module);
            return this;
        }

        @Override
        public <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(Class<Type> clazz) {
            return new AnnotatedBindingConfiguration<Type>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                    addBinding(configuration);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(clazz);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(clazz);
                }
            };
        }

        public <Type> LinkedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(Key<Type> key) {
            return new LinkedBindingConfiguration<Type, Configuration<?>>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                    addBinding(configuration);
                }

                @Override
                public Void expose(PrivateBinder binder) {
                    binder.expose(key);
                    return null;
                }

                @Override
                public LinkedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(key);
                }
            };
        }

        public <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> exposeAndBind(TypeLiteral<Type> literal) {
            return new AnnotatedBindingConfiguration<Type>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                    addBinding(configuration);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(literal);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(literal);
                }
            };
        }

        @Override
        public AnnotatedElementDefinition<? extends ModuleBuilder4Guice<Parent>> expose(Class<?> clazz) {
            return new AnnotatedElementConfiguration<Configuration<?>>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(clazz);
                }
            };
        }

        public Definition<? extends ModuleBuilder4Guice<Parent>> expose(Key<?> key) {
            return new Configuration<Configuration<?>>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                }

                @Override
                public Void expose(PrivateBinder binder) {
                    binder.expose(key);
                    return null;
                }
            };
        }

        public AnnotatedElementDefinition<? extends ModuleBuilder4Guice<Parent>> expose(TypeLiteral<?> literal) {
            return new AnnotatedElementConfiguration<Configuration<?>>() {

                @Override
                void add(Configuration<?> configuration) {
                    addExposing(configuration);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(literal);
                }
            };
        }

        @Override
        public <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(Class<Type> clazz) {
            return new AnnotatedBindingConfiguration<Type>() {

                @Override
                void add(Configuration<?> configuration) {
                    addBinding(configuration);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(clazz);
                }
            };
        }

        public <Type> LinkedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(Key<Type> key) {
            return new LinkedBindingConfiguration<Type, Configuration<?>>() {

                @Override
                void add(Configuration<?> configuration) {
                    addBinding(configuration);
                }

                @Override
                public LinkedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(key);
                }
            };
        }

        public <Type> AnnotatedBindingDefinition4Guice<Type, ? extends ModuleBuilder4Guice<Parent>> bind(TypeLiteral<Type> literal) {
            return new AnnotatedBindingConfiguration<Type>() {

                @Override
                void add(Configuration<?> configuration) {
                    addBinding(configuration);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(literal);
                }
            };
        }

        @Override
        public AnnotatedConstantBindingDefinition<? extends ModuleBuilder4Guice<Parent>> bindConstant() {
            return new AnnotatedConstantBindingConfiguration() {

                @Override
                void add(Configuration<?> configuration) {
                    addBinding(configuration);
                }

                @Override
                public AnnotatedConstantBindingBuilder bind(Binder binder) {
                    return binder.bindConstant();
                }
            };
        }

        void addExposing(Configuration<?> exposing) {
            exposings.add(exposing);
        }

        private void addBinding(Configuration<?> binding) {
            bindings.add(binding);
        }

        @Override
        public Module build() {
            final List<Configuration<?>> exposings = swapExposings();
            if (exposings.isEmpty()) {
                return new AbstractModule() {

                    @Override
                    protected void configure() { installTo(binder()); }
                };
            } else {
                return new PrivateModule() {

                    @Override
                    protected void configure() {
                        final PrivateBinder binder = binder();
                        for (Configuration exposing : exposings) {
                            exposing.expose(binder);
                        }
                        installTo(binder);
                    }
                };
            }
        }

        void installTo(final Binder binder) {
            for (Configuration<?> binding : swapBindings()) {
                binding.bind(binder);
            }
            for (Module module : swapModules()) {
                binder.install(module);
            }
        }

        private List<Module> swapModules() {
            try {
                return this.modules;
            } finally {
                this.modules = newList();
            }
        }

        private List<Configuration<?>> swapExposings() {
            try {
                return this.exposings;
            } finally {
                this.exposings = newList();
            }
        }

        private List<Configuration<?>> swapBindings() {
            try {
                return this.bindings;
            } finally {
                this.bindings = newList();
            }
        }

        private abstract class AnnotatedBindingConfiguration<Type>
                extends LinkedBindingConfiguration<Type, Configuration<?>>
                implements AnnotatedBindingDefinition4Guice<Type, ModuleBuilder4Guice<Parent>> {

            @Override
            AnnotatedElementBuilder expose(PrivateBinder binder) {
                throw new AssertionError();
            }

            @Override
            abstract AnnotatedBindingBuilder<Type> bind(Binder binder);

            @Override
            public LinkedBindingDefinition4Guice<Type, ModuleBuilder4Guice<Parent>> annotatedWith(Class<? extends Annotation> annotationType) {
                return new ChildConfiguration() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        parentExpose(binder).annotatedWith(annotationType);
                        return null;
                    }

                    @Override
                    LinkedBindingBuilder<Type> bind(Binder input) {
                        return parentBind(input).annotatedWith(annotationType);
                    }
                };
            }

            @Override
            public LinkedBindingDefinition4Guice<Type, ModuleBuilder4Guice<Parent>> annotatedWith(Annotation annotation) {
                return new ChildConfiguration() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        parentExpose(binder).annotatedWith(annotation);
                        return null;
                    }

                    @Override
                    LinkedBindingBuilder<Type> bind(Binder input) {
                        return parentBind(input).annotatedWith(annotation);
                    }
                };
            }

            abstract class ChildConfiguration
                    extends LinkedBindingConfiguration<Type, AnnotatedBindingConfiguration<Type>> {

                AnnotatedElementBuilder parentExpose(PrivateBinder binder) {
                    return parent().expose(binder);
                }

                AnnotatedBindingBuilder<Type> parentBind(Binder binder) {
                    return parent().bind(binder);
                }

                @Override
                AnnotatedBindingConfiguration<Type> parent() {
                    return AnnotatedBindingConfiguration.this;
                }
            }
        }

        private abstract class LinkedBindingConfiguration<Type, ParentConfiguration extends Configuration<?>>
                extends ScopedBindingConfiguration<ParentConfiguration>
                implements LinkedBindingDefinition4Guice<Type, ModuleBuilder4Guice<Parent>> {

            @Override
            abstract LinkedBindingBuilder<Type> bind(Binder binder);

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> to(Class<? extends Type> implementation) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).to(implementation);
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> to(TypeLiteral<? extends Type> implementation) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).to(implementation);
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> to(Key<? extends Type> targetKey) {
                return new ChildConfiguration() {
                    @Override ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).to(targetKey);
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> toInstance(Type instance) {
                return new Configuration<LinkedBindingConfiguration<Type, ParentConfiguration>>() {

                    @Override LinkedBindingConfiguration<Type, ParentConfiguration> parent() {
                        return LinkedBindingConfiguration.this;
                    }

                    @Override
                    Void expose(PrivateBinder binder) {
                        parent().expose(binder);
                        return null;
                    }

                    @Override
                    Void bind(Binder binder) {
                        parent().bind(binder).toInstance(instance);
                        return null;
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toProvider(Provider<? extends Type> provider) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toProvider(provider);
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toProvider(Class<? extends Provider<? extends Type>> providerType) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toProvider(providerType);
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toProvider(TypeLiteral<? extends Provider<? extends Type>> providerType) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toProvider(providerType);
                    }
                };
            }

            @Override
            public ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toProvider(Key<? extends Provider<? extends Type>> providerKey) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toProvider(providerKey);
                    }
                };
            }

            @Override
            public <S extends Type> ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toConstructor(Constructor<S> constructor) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toConstructor(constructor);
                    }
                };
            }

            @Override
            public <S extends Type> ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
                return new ChildConfiguration() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return parentBind(binder).toConstructor(constructor, type);
                    }
                };
            }

            abstract class ChildConfiguration
                    extends ScopedBindingConfiguration<ScopedBindingConfiguration<ParentConfiguration>> {

                @Override
                Void expose(PrivateBinder binder) {
                    parent().expose(binder);
                    return null;
                }

                LinkedBindingBuilder<Type> parentBind(Binder binder) {
                    return parent().bind(binder);
                }

                @Override
                LinkedBindingConfiguration<Type, ParentConfiguration> parent() {
                    return LinkedBindingConfiguration.this;
                }
            }
        }

        private abstract class ScopedBindingConfiguration<ParentConfiguration extends Configuration<?>>
                extends Configuration<ParentConfiguration>
                implements ScopedBindingDefinition4Guice<ModuleBuilder4Guice<Parent>> {

            @Override
            abstract ScopedBindingBuilder bind(Binder binder);

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> in(Class<? extends Annotation> scopeAnnotation) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).in(scopeAnnotation);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> in(Scope scope) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).in(scope);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> asEagerSingleton() {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).asEagerSingleton();
                        return null;
                    }
                };
            }

            abstract class ChildConfiguration
                    extends Configuration<ScopedBindingConfiguration<ParentConfiguration>> {

                @Override
                Void expose(PrivateBinder binder) {
                    parent().expose(binder);
                    return null;
                }

                ScopedBindingBuilder parentBind(Binder binder) {
                    return parent().bind(binder);
                }

                @Override
                ScopedBindingConfiguration<ParentConfiguration> parent() {
                    return ScopedBindingConfiguration.this;
                }
            }
        }

        private abstract class AnnotatedElementConfiguration<ParentConfiguration extends Configuration<?>>
                extends Configuration<ParentConfiguration>
                implements AnnotatedElementDefinition<ModuleBuilder4Guice<Parent>> {

            @Override
            abstract AnnotatedElementBuilder expose(PrivateBinder binder);

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> annotatedWith(Class<? extends Annotation> annotationType) {
                return new ChildConfiguration() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        parentExpose(binder).annotatedWith(annotationType);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> annotatedWith(Annotation annotation) {
                return new ChildConfiguration() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        parentExpose(binder).annotatedWith(annotation);
                        return null;
                    }
                };
            }

            abstract class ChildConfiguration
                    extends Configuration<AnnotatedElementConfiguration<ParentConfiguration>> {

                AnnotatedElementBuilder parentExpose(PrivateBinder binder) {
                    return parent().expose(binder);
                }

                @Override
                AnnotatedElementConfiguration<ParentConfiguration> parent() {
                    return AnnotatedElementConfiguration.this;
                }
            }
        }

        private abstract class AnnotatedConstantBindingConfiguration
                extends Configuration<Configuration<?>>
                implements AnnotatedConstantBindingDefinition<ModuleBuilder4Guice<Parent>> {

            @Override
            abstract AnnotatedConstantBindingBuilder bind(Binder binder);

            @Override
            public ConstantBindingDefinition<ModuleBuilder4Guice<Parent>> annotatedWith(Class<? extends Annotation> annotationType) {
                return new ChildConfiguration() {

                    @Override ConstantBindingBuilder bind(Binder binder) {
                        return parentBind(binder).annotatedWith(annotationType);
                    }
                };
            }

            @Override
            public ConstantBindingDefinition<ModuleBuilder4Guice<Parent>> annotatedWith(Annotation annotation) {
                return new ChildConfiguration() {

                    @Override
                    ConstantBindingBuilder bind(Binder binder) {
                        return parentBind(binder).annotatedWith(annotation);
                    }
                };
            }

            abstract class ChildConfiguration
                    extends ConstantBindingConfiguration<AnnotatedConstantBindingConfiguration> {

                AnnotatedConstantBindingBuilder parentBind(Binder binder) {
                    return parent().bind(binder);
                }

                @Override
                AnnotatedConstantBindingConfiguration parent() {
                    return AnnotatedConstantBindingConfiguration.this;
                }
            }
        }

        private abstract class ConstantBindingConfiguration<ParentConfiguration extends Configuration<?>>
                extends Configuration<ParentConfiguration>
                implements ConstantBindingDefinition<ModuleBuilder4Guice<Parent>> {

            @Override
            abstract ConstantBindingBuilder bind(Binder binder);

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(String value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(int value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(long value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(boolean value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(double value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(float value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(short value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(char value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(byte value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Definition<ModuleBuilder4Guice<Parent>> to(Class<?> value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public <E extends Enum<E>> Definition<ModuleBuilder4Guice<Parent>> to(E value) {
                return new ChildConfiguration() {

                    @Override
                    Void bind(Binder binder) {
                        parentBind(binder).to(value);
                        return null;
                    }
                };
            }

            abstract class ChildConfiguration
                    extends Configuration<ConstantBindingConfiguration<ParentConfiguration>> {

                ConstantBindingBuilder parentBind(Binder binder) {
                    return parent().bind(binder);
                }

                @Override
                ConstantBindingConfiguration<ParentConfiguration> parent() {
                    return ConstantBindingConfiguration.this;
                }
            }
        }

        private abstract class Configuration<ParentConfiguration extends Configuration<?>>
                implements Definition<ModuleBuilder4Guice<Parent>> {

            @Override
            public ModuleBuilder4Guice<Parent> end() {
                add(this);
                return ModuleBuilder4Guice.this;
            }

            void add(Configuration<?> configuration) {
                parent().add(configuration);
            }

            ParentConfiguration parent() { throw new AssertionError(); }

            Object expose(PrivateBinder binder) { throw new AssertionError(); }

            Object bind(Binder binder) { throw new AssertionError(); }
        }
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
