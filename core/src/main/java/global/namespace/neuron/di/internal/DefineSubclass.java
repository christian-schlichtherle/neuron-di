package global.namespace.neuron.di.internal;

interface DefineSubclass {

    <C> Class<? extends C> apply(Class<C> clazz, String name, byte[] b);
}
