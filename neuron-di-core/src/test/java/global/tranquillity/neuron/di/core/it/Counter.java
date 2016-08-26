package global.tranquillity.neuron.di.core.it;

public class Counter {

    int count;

    Counter inc() {
        count++;
        return this;
    }
}
