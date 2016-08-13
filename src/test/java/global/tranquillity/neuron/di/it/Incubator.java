package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Organism;

interface Incubator {

    default Organism breedOrganism() { return Organism.breed(); }
}
