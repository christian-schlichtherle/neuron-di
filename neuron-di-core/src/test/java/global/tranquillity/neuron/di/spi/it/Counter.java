package global.tranquillity.neuron.di.spi.it;

public class Counter {

    public int count;

    public Counter inc() {
        count++;
        return this;
    }
}
