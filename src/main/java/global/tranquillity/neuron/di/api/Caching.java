package global.tranquillity.neuron.di.api;

import java.lang.annotation.*;

/** Requests that the return value of the annotated method gets cached. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Caching {

    /** Configures the caching strategy to apply. */
    CachingStrategy value() default CachingStrategy.THREAD_SAFE;
}
