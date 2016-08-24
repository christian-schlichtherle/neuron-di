package global.tranquillity.neuron.di.core;

import global.tranquillity.neuron.di.api.CachingStrategy;
import global.tranquillity.neuron.di.api.Neuron;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

class Inspection {

    private final Class<?> runtimeClass;

    private Inspection(final Class<?> runtimeClass) {
        this.runtimeClass = runtimeClass;
    }

    static Inspection of(Class<?> runtimeClass) {
        return new Inspection(runtimeClass);
    }

    Collection<Method> synapses() {
        final LinkedList<Method> synapses = new LinkedList<>();
        element().accept(new Visitor() {
            @Override
            public void visitSynapse(SynapseElement element) {
                synapses.add(element.method());
            }
        });
        return synapses;
    }

    void accept(Visitor visitor) { element().accept(visitor); }

    private Element element() {

        class RealClassElement implements ClassElement {

            public Class<?> runtimeClass() { return runtimeClass; }
        }

        final Neuron neuron = runtimeClass.getAnnotation(Neuron.class);
        if (null != neuron) {

            class RealNeuronElement
                    extends RealClassElement
                    implements NeuronElement {

                @Override
                public CachingStrategy cachingStrategy() {
                    return neuron.cachingStrategy();
                }
            }

            return new RealNeuronElement();
        } else {
            return new RealClassElement();
        }
    }
}
