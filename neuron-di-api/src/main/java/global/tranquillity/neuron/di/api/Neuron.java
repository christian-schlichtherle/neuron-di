package global.tranquillity.neuron.di.api;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Neuron {

    /**
     * Configures the strategy for caching the return value of abstract methods
     * which are not annotated with {@link Caching}.
     */
    CachingStrategy cachingStrategy() default CachingStrategy.THREAD_SAFE;
}
