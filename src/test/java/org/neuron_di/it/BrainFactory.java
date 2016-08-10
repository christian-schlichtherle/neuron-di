package org.neuron_di.it;

import org.neuron_di.api.Brain;

interface BrainFactory {

    default Brain newBrain() { return Brain.build(); }
}
