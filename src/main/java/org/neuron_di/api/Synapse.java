package org.neuron_di.api;

import java.lang.annotation.*;

/**
 * Configures the behavior of a synapse.
 * A synapse is an injection point for a dependency which is declared by an
 * abstract method in a {@link Neuron} annotated type.
 * You only need to use this annotation if you want to alter the behavior of a
 * synapse from its defaults, which are defined by its members below.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Synapse {

    /**
     * Defines the caching strategy for the dependency, that is, the return
     * value of the annotated method.
     */
    CachingStrategy cachingStrategy() default CachingStrategy.THREAD_SAFE;
}
