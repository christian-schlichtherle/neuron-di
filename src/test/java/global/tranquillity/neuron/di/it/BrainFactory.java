package global.tranquillity.neuron.di.it;

import global.tranquillity.neuron.di.api.Brain;

interface BrainFactory {

    default Brain newBrain() { return Brain.build(); }
}
