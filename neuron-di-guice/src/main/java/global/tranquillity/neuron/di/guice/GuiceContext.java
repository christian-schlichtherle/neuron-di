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

    public InjectorConfiguration injector() {
        return new InjectorConfiguration();
    }

    private static <T> List<T> newList() { return new LinkedList<T>(); }

    public class InjectorConfiguration extends InjectorBuilder {

        private List<Module> modules = newList();

        @Override
        public ModuleConfiguration<InjectorConfiguration> module() {
            return new ModuleConfiguration<InjectorConfiguration>() {

                @Override
                public InjectorConfiguration end() {
                    return InjectorConfiguration.this.module(build());
                }
            };
        }

        @Override
        public InjectorConfiguration module(Module module) {
            modules.add(module);
            return this;
        }

        @Override
        public Injector build() { return Guice.createInjector(swapModules()); }

        private List<Module> swapModules() {
            try {
                return this.modules;
            } finally {
                this.modules = newList();
            }
        }
    }

    public abstract class ModuleConfiguration<Parent> extends ModuleBuilder<Parent> {

        private List<Module> submodules = newList();
        private List<Step> exposings = newList();
        private List<Step> bindings = newList();

        @Override
        public ModuleConfiguration<ModuleConfiguration<Parent>> module() {
            return new ModuleConfiguration<ModuleConfiguration<Parent>>() {

                @Override
                public ModuleConfiguration<Parent> end() {
                    return ModuleConfiguration.this.module(build());
                }
            };
        }

        @Override
        public ModuleConfiguration<Parent> module(Module module) {
            submodules.add(module);
            return this;
        }

        @Override
        public <Type> AnnotatedBindingStep<Type> exposeAndBind(Class<Type> clazz) {
            return new AnnotatedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addExposing(step);
                    addBinding(step);
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

        public <Type> LinkedBindingStep<Type> exposeAndBind(Key<Type> key) {
            return new LinkedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addExposing(step);
                    addBinding(step);
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

        public <Type> AnnotatedBindingStep<Type> exposeAndBind(TypeLiteral<Type> literal) {
            return new AnnotatedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addExposing(step);
                    addBinding(step);
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
        public AnnotatedElementStep expose(Class<?> clazz) {
            return new AnnotatedElementStep() {

                @Override
                void add(Step step) {
                    addExposing(step);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(clazz);
                }
            };
        }

        public Step expose(Key<?> key) {
            return new Step() {

                @Override
                void add(Step step) {
                    addExposing(step);
                }

                @Override
                public Void expose(PrivateBinder binder) {
                    binder.expose(key);
                    return null;
                }
            };
        }

        public AnnotatedElementStep expose(TypeLiteral<?> literal) {
            return new AnnotatedElementStep() {

                @Override
                void add(Step step) {
                    addExposing(step);
                }

                @Override
                public AnnotatedElementBuilder expose(PrivateBinder binder) {
                    return binder.expose(literal);
                }
            };
        }

        @Override
        public <Type> AnnotatedBindingStep<Type> bind(Class<Type> clazz) {
            return new AnnotatedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addBinding(step);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(clazz);
                }
            };
        }

        public <Type> LinkedBindingStep<Type> bind(Key<Type> key) {
            return new LinkedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addBinding(step);
                }

                @Override
                public LinkedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(key);
                }
            };
        }

        public <Type> AnnotatedBindingStep<Type> bind(TypeLiteral<Type> literal) {
            return new AnnotatedBindingStep<Type>() {

                @Override
                void add(Step step) {
                    addBinding(step);
                }

                @Override
                public AnnotatedBindingBuilder<Type> bind(Binder binder) {
                    return binder.bind(literal);
                }
            };
        }

        @Override
        public AnnotatedConstantBindingStep bindConstant() {
            return new AnnotatedConstantBindingStep() {

                @Override
                void add(Step step) {
                    addBinding(step);
                }

                @Override
                public AnnotatedConstantBindingBuilder bind(Binder binder) {
                    return binder.bindConstant();
                }
            };
        }

        private void addExposing(Step exposing) {
            exposings.add(exposing);
        }

        private void addBinding(Step binding) {
            bindings.add(binding);
        }

        @Override
        public Module build() {
            final List<Step> exposings = swapExposings();
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
                        for (Step exposing : exposings) {
                            exposing.expose(binder);
                        }
                        installTo(binder);
                    }
                };
            }
        }

        private void installTo(final Binder binder) {
            for (Step binding : swapBindings()) {
                binding.bind(binder);
            }
            for (Module module : swapModules()) {
                binder.install(module);
            }
        }

        private List<Module> swapModules() {
            try {
                return this.submodules;
            } finally {
                this.submodules = newList();
            }
        }

        private List<Step> swapExposings() {
            try {
                return this.exposings;
            } finally {
                this.exposings = newList();
            }
        }

        private List<Step> swapBindings() {
            try {
                return this.bindings;
            } finally {
                this.bindings = newList();
            }
        }

        public abstract class AnnotatedBindingStep<Type>
                extends LinkedBindingStep<Type>
                implements AnnotatedBindingDefinition<Type, ModuleConfiguration<Parent>> {

            @Override
            AnnotatedElementBuilder expose(PrivateBinder binder) {
                throw new AssertionError();
            }

            @Override
            abstract AnnotatedBindingBuilder<Type> bind(Binder binder);

            @Override
            public LinkedBindingStep<Type> annotatedWith(Class<? extends Annotation> annotationType) {
                return new NextStep() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        previousStepExpose(binder).annotatedWith(annotationType);
                        return null;
                    }

                    @Override
                    LinkedBindingBuilder<Type> bind(Binder binder) {
                        return previousStepBind(binder).annotatedWith(annotationType);
                    }
                };
            }

            @Override
            public LinkedBindingStep<Type> annotatedWith(Annotation annotation) {
                return new NextStep() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        previousStepExpose(binder).annotatedWith(annotation);
                        return null;
                    }

                    @Override
                    LinkedBindingBuilder<Type> bind(Binder binder) {
                        return previousStepBind(binder).annotatedWith(annotation);
                    }
                };
            }

            abstract class NextStep extends LinkedBindingStep<Type> {

                AnnotatedElementBuilder previousStepExpose(PrivateBinder binder) {
                    return previousStep().expose(binder);
                }

                AnnotatedBindingBuilder<Type> previousStepBind(Binder binder) {
                    return previousStep().bind(binder);
                }

                @Override
                AnnotatedBindingStep<Type> previousStep() {
                    return AnnotatedBindingStep.this;
                }
            }
        }

        public abstract class LinkedBindingStep<Type>
                extends ScopedBindingStep
                implements LinkedBindingDefinition<Type, ModuleConfiguration<Parent>> {

            @Override
            abstract LinkedBindingBuilder<Type> bind(Binder binder);

            @Override
            public ScopedBindingStep to(Class<? extends Type> implementation) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).to(implementation);
                    }
                };
            }

            public ScopedBindingStep to(TypeLiteral<? extends Type> implementation) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).to(implementation);
                    }
                };
            }

            public ScopedBindingStep to(Key<? extends Type> targetKey) {
                return new NextStep() {
                    @Override ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).to(targetKey);
                    }
                };
            }

            @Override
            public Step toInstance(Type instance) {
                return new Step() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        previousStep().expose(binder);
                        return null;
                    }

                    @Override
                    Void bind(Binder binder) {
                        previousStep().bind(binder).toInstance(instance);
                        return null;
                    }

                    @Override
                    LinkedBindingStep<Type> previousStep() {
                        return LinkedBindingStep.this;
                    }
                };
            }

            @Override
            public ScopedBindingStep toProvider(Provider<? extends Type> provider) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toProvider(provider);
                    }
                };
            }

            @Override
            public ScopedBindingStep toProvider(Class<? extends Provider<? extends Type>> providerType) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toProvider(providerType);
                    }
                };
            }

            public ScopedBindingStep toProvider(TypeLiteral<? extends Provider<? extends Type>> providerType) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toProvider(providerType);
                    }
                };
            }

            public ScopedBindingStep toProvider(Key<? extends Provider<? extends Type>> providerKey) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toProvider(providerKey);
                    }
                };
            }

            @Override
            public <S extends Type> ScopedBindingStep toConstructor(Constructor<S> constructor) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toConstructor(constructor);
                    }
                };
            }

            public <S extends Type> ScopedBindingStep toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
                return new NextStep() {

                    @Override
                    ScopedBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).toConstructor(constructor, type);
                    }
                };
            }

            private abstract class NextStep extends ScopedBindingStep {

                @Override
                Void expose(PrivateBinder binder) {
                    previousStep().expose(binder);
                    return null;
                }

                LinkedBindingBuilder<Type> previousStepBind(Binder binder) {
                    return previousStep().bind(binder);
                }

                @Override
                LinkedBindingStep<Type> previousStep() {
                    return LinkedBindingStep.this;
                }
            }
        }

        public abstract class ScopedBindingStep
                extends Step
                implements ScopedBindingDefinition<ModuleConfiguration<Parent>> {

            @Override
            abstract ScopedBindingBuilder bind(Binder binder);

            @Override
            public Step in(Class<? extends Annotation> scopeAnnotation) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).in(scopeAnnotation);
                        return null;
                    }
                };
            }

            public Step in(Scope scope) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).in(scope);
                        return null;
                    }
                };
            }

            @Override
            public Step asEagerSingleton() {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).asEagerSingleton();
                        return null;
                    }
                };
            }

            private abstract class NextStep extends Step {

                @Override
                Void expose(PrivateBinder binder) {
                    previousStep().expose(binder);
                    return null;
                }

                ScopedBindingBuilder previousStepBind(Binder binder) {
                    return previousStep().bind(binder);
                }

                @Override
                ScopedBindingStep previousStep() {
                    return ScopedBindingStep.this;
                }
            }
        }

        public abstract class AnnotatedElementStep
                extends Step
                implements AnnotatedElementDefinition<ModuleConfiguration<Parent>> {

            @Override
            abstract AnnotatedElementBuilder expose(PrivateBinder binder);

            @Override
            public Step annotatedWith(Class<? extends Annotation> annotationType) {
                return new NextStep() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        previousStepExpose(binder).annotatedWith(annotationType);
                        return null;
                    }
                };
            }

            @Override
            public Step annotatedWith(Annotation annotation) {
                return new NextStep() {

                    @Override
                    Void expose(PrivateBinder binder) {
                        previousStepExpose(binder).annotatedWith(annotation);
                        return null;
                    }
                };
            }

            private abstract class NextStep extends Step {

                AnnotatedElementBuilder previousStepExpose(PrivateBinder binder) {
                    return previousStep().expose(binder);
                }

                @Override
                AnnotatedElementStep previousStep() {
                    return AnnotatedElementStep.this;
                }
            }
        }

        public abstract class AnnotatedConstantBindingStep
                extends Step
                implements AnnotatedConstantBindingDefinition<ModuleConfiguration<Parent>> {

            @Override
            abstract AnnotatedConstantBindingBuilder bind(Binder binder);

            @Override
            public ConstantBindingStep annotatedWith(Class<? extends Annotation> annotationType) {
                return new NextStep() {

                    @Override ConstantBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).annotatedWith(annotationType);
                    }
                };
            }

            @Override
            public ConstantBindingStep annotatedWith(Annotation annotation) {
                return new NextStep() {

                    @Override
                    ConstantBindingBuilder bind(Binder binder) {
                        return previousStepBind(binder).annotatedWith(annotation);
                    }
                };
            }

            private abstract class NextStep extends ConstantBindingStep {

                AnnotatedConstantBindingBuilder previousStepBind(Binder binder) {
                    return previousStep().bind(binder);
                }

                @Override
                AnnotatedConstantBindingStep previousStep() {
                    return AnnotatedConstantBindingStep.this;
                }
            }
        }

        public abstract class ConstantBindingStep
                extends Step
                implements ConstantBindingDefinition<ModuleConfiguration<Parent>> {

            @Override
            abstract ConstantBindingBuilder bind(Binder binder);

            @Override
            public Step to(String value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(int value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(long value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(boolean value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(double value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(float value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(short value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(char value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(byte value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public Step to(Class<?> value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            @Override
            public <E extends Enum<E>> Step to(E value) {
                return new NextStep() {

                    @Override
                    Void bind(Binder binder) {
                        previousStepBind(binder).to(value);
                        return null;
                    }
                };
            }

            private abstract class NextStep extends Step {

                ConstantBindingBuilder previousStepBind(Binder binder) {
                    return previousStep().bind(binder);
                }

                @Override
                ConstantBindingStep previousStep() {
                    return ConstantBindingStep.this;
                }
            }
        }

        public abstract class Step
                implements Definition<ModuleConfiguration<Parent>> {

            @Override
            public ModuleConfiguration<Parent> end() {
                add(this);
                return ModuleConfiguration.this;
            }

            void add(Step step) { previousStep().add(step); }

            Object expose(PrivateBinder binder) { throw new AssertionError(); }

            Object bind(Binder binder) { throw new AssertionError(); }

            Step previousStep() { throw new AssertionError(); }
        }
    }
}
