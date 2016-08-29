package global.namespace.neuron.di.spi;

import global.namespace.neuron.di.api.CachingStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.FixedValue;

interface HasCachingStrategy {

    default Callback synapseCallback(FixedValue callback) {
        return realCachingStrategy().synapseCallback(callback);
    }

    default Callback methodCallback() {
        return realCachingStrategy().methodCallback();
    }

    default RealCachingStrategy realCachingStrategy() {
        return RealCachingStrategy.valueOf(cachingStrategy());
    }

    CachingStrategy cachingStrategy();
}
