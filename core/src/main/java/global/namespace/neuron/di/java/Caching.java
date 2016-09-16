/*
 * Copyright © 2016 Schlichtherle IT Services
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

import java.lang.annotation.*;

/**
 * Indicates that a caching strategy should be applied to the return value of
 * the annotated method.
 * <p>
 * In compliance with the Java Language Specification, this annotation is not
 * inherited, so if you override a method with this annotation and you want to
 * apply a caching strategy to the overriding method, you have to add this
 * annotation again.
 * In other words, if this annotation is not present on a method, no caching
 * strategy is applied to it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Caching {

    /**
     * Configures the caching strategy to apply to the return value of the
     * annotated method.
     */
    CachingStrategy value() default CachingStrategy.THREAD_SAFE;
}
