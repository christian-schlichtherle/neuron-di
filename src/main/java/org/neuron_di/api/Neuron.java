package org.neuron_di.api;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Neuron {

    /**
     * Defines the default caching strategy for the dependencies, that is, the
     * return value of the abstract methods.
     */
    CachingStrategy cachingStrategy() default CachingStrategy.THREAD_SAFE;
}
