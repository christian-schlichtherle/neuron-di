package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Organism;

interface Incubator {

    default Organism breed() { return Organism.breed(); }
}
