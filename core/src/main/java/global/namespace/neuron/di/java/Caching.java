/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package global.namespace.neuron.di.java;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Indicates that a caching strategy should be applied to the return value of the annotated synapse method.
 * <p>
 * In compliance with the Java Language Specification, this annotation is not inherited, so if you override a method
 * with this annotation and you want to apply a caching strategy to the overriding method, you have to add this
 * annotation again.
 * In other words, if this annotation is not present on a synapse method, no caching strategy is applied to it.
 * <p>
 * This annotation can also be applied to custom annotation types, in which case any method annotated with these is
 * treated as if it's directly annotated with this annotation.
 * For example:
 * <pre><code>
 *     import global.namespace.neuron.di.java.*;
 *     import java.lang.annotation.*;
 *
 *     &#64;Caching(CachingStrategy.THREAD_LOCAL)
 *     &#64;Documented
 *     &#64;Retention(RetentionPolicy.RUNTIME)
 *     &#64;Target(ElementType.METHOD)
 *     public &#64;interface ThreadLocalCaching {
 *     }
 * </code></pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ANNOTATION_TYPE, METHOD})
public @interface Caching {

    /**
     * Configures the caching strategy to apply to the return value of the
     * annotated method.
     */
    CachingStrategy value() default CachingStrategy.THREAD_SAFE;
}
