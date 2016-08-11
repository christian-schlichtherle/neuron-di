package org.neuron_di.api;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Neuron {

    /**
     * Configures the strategy for caching the return value of methods which
     * require caching, but are not annotated with {@link Caching}.
     */
    CachingStrategy caching() default CachingStrategy.THREAD_SAFE;
}
