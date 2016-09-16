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
 * Indicates that the return values of any abstract, parameterless methods
 * are subject to lazy dependency resolution by the {@link Incubator}.
 * Abstract, parameterless methods in classes or interfaces annotated with
 * {@code @Neuron} are generally called "synapse methods".
 * <p>
 * If this annotation is present on a class, it gets inherited by any subclass.
 * In compliance with the Java Language Specification, this does not apply to
 * interfaces.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Neuron {

    /**
     * Configures the strategy for caching the return value of synapse methods
     * which are not annotated with {@link Caching}.
     */
    CachingStrategy cachingStrategy() default CachingStrategy.DISABLED;
}
